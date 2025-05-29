import asyncio
import json
import logging
import os
import tempfile
import time
import uuid
from typing import Any, AsyncIterable, Optional

import google.auth
from google import genai
from google.adk.agents.llm_agent import LlmAgent
from google.adk.artifacts import InMemoryArtifactService # Kept for ADK structure consistency
from google.adk.memory.in_memory_memory_service import InMemoryMemoryService # Kept
from google.adk.runners import Runner # Kept
from google.adk.sessions import InMemorySessionService # Kept
from google.cloud import storage
from google.genai import types as genai_types


logger = logging.getLogger(__name__)

class VideoGenerationAgent:
    """
    An agent that generates video from a text prompt using VEO,
    providing periodic updates and a final GCS URL for the video.
    """

    SUPPORTED_INPUT_CONTENT_TYPES = ['text', 'text/plain']
    SUPPORTED_OUTPUT_CONTENT_TYPES = ['text/plain', 'video/mp4']

    VEO_MODEL_NAME = os.getenv("VEO_MODEL_NAME", "veo-2.0-generate-001")
    VEO_POLLING_INTERVAL_SECONDS = int(os.getenv("VEO_POLLING_INTERVAL_SECONDS", "5"))
    VEO_SIMULATED_TOTAL_GENERATION_TIME_SECONDS = int(os.getenv("VEO_SIMULATED_TOTAL_GENERATION_TIME_SECONDS", "120")) # 2 minutes for simulated progress
    VEO_DEFAULT_PERSON_GENERATION = "dont_allow"
    VEO_DEFAULT_ASPECT_RATIO = "16:9"

    GCS_BUCKET_NAME_ENV_VAR = "VIDEO_GEN_GCS_BUCKET"
    SIGNED_URL_EXPIRATION_SECONDS = 3600*48
    SIGNER_SERVICE_ACCOUNT_EMAIL_ENV_VAR = "SIGNER_SERVICE_ACCOUNT_EMAIL"

    def __init__(self):
        logger.info("Initializing VideoGenerationAgent...")
        try:
            self.genai_client = genai.Client()
            logger.info("Google GenAI client initialized.")
        except Exception as e:
            logger.error(f"Failed to initialize Google GenAI client: {e}")
            self.genai_client = None
            raise

        self.gcs_bucket_name = os.getenv(self.GCS_BUCKET_NAME_ENV_VAR)
        if not self.gcs_bucket_name:
            logger.error(
                f"{self.GCS_BUCKET_NAME_ENV_VAR} environment variable not set. "
                "Video upload to GCS will not be possible."
            )
            raise
        elif not storage:
            logger.error(
                "google-cloud-storage library not found, but GCS bucket is set. "
                "Video upload to GCS will fail. Please install google-cloud-storage."
            )
            raise
        else:
            try:
                self.credentials, self.project_id = google.auth.default(
                    scopes=['https://www.googleapis.com/auth/cloud-platform']
                )
                logger.info(f"Successfully obtained ADC for GCS.")
                self.storage_client = storage.Client(credentials=self.credentials, project=self.project_id)
                logger.info("Google Cloud Storage client initialized.")
            except google.auth.exceptions.DefaultCredentialsError:
                logger.error(
                    "Could not get Application Default Credentials for GCS. "
                    "Please run 'gcloud auth application-default login' or set GOOGLE_APPLICATION_CREDENTIALS."
                )
                raise
            except Exception as e:
                logger.error(f"Failed to initialize Google Cloud Storage client: {e}")
                raise
        
        sa_email_from_env = os.getenv(self.SIGNER_SERVICE_ACCOUNT_EMAIL_ENV_VAR)
        self.signer_service_account_email = sa_email_from_env.strip('\'"') if sa_email_from_env else None
        if self.signer_service_account_email:
            logger.info(f"Will use service account '{self.signer_service_account_email}' for signing GCS URLs.")
        else:
            logger.info("No SIGNER_SERVICE_ACCOUNT_EMAIL set. Will use ambient gcloud credentials for signing GCS URLs.")


        self._agent_llm = self._build_llm_agent()
        self._user_id = 'video_generation_adk_user' # Static user ID for ADK session context
        self._runner = Runner(
            app_name=self._agent_llm.name,
            agent=self._agent_llm,
            artifact_service=InMemoryArtifactService(),
            session_service=InMemorySessionService(),
            memory_service=InMemoryMemoryService(),
        )
        logger.info("VideoGenerationAgent initialized.")

    def _build_llm_agent(self) -> LlmAgent:
        """Builds LLM agent"""
        return LlmAgent(
            model='gemini-2.0-flash-001',
            name='video_generation_orchestrator',
            description='This agent orchestrates video generation from text prompts using VEO.',
            instruction="""\
            You are an assistant that helps kick off video generation tasks.
            When a user provides a prompt for a video, acknowledge the request
            and state that the video generation process will begin.
            The actual video generation and progress updates are handled by the system.
            Do not attempt to generate the video yourself or call any tools.
            """,
            tools=[],
        )

    async def _upload_bytes_to_gcs(self, video_bytes: bytes, bucket_name: str, blob_name: str, content_type: str = 'video/mp4') -> str:
        if not self.storage_client:
            raise RuntimeError("Google Cloud Storage client not initialized. Cannot upload video.")
        
        bucket = self.storage_client.bucket(bucket_name)
        blob = bucket.blob(blob_name)
        
        temp_dir = tempfile.gettempdir()
        temp_file_path = os.path.join(temp_dir, blob_name.split('/')[-1])

        try:
            with open(temp_file_path, 'wb') as f:
                f.write(video_bytes)
            logger.info(f"Attempting to upload video from temp file {temp_file_path} to gs://{bucket_name}/{blob_name} with content_type: {content_type}")
            blob.upload_from_filename(temp_file_path, content_type=content_type)
        finally:
            if os.path.exists(temp_file_path):
                os.remove(temp_file_path)
        
        gcs_uri = f"gs://{bucket_name}/{blob_name}"
        logger.info(f"Successfully uploaded video to {gcs_uri}")
        return gcs_uri

    async def _generate_signed_url(self, blob_name: str, bucket_name: str, expiration_seconds: int) -> str:

        bucket = self.storage_client.bucket(bucket_name)
        blob = bucket.blob(blob_name)

        try:
            signed_url = blob.generate_signed_url(
                version="v4",
                expiration=expiration_seconds,
                method="GET",
                service_account_email=self.signer_service_account_email  # None if not set, uses ambient creds
            )
            logger.info(f"Successfully generated signed URL for gs://{bucket_name}/{blob_name}")
            return signed_url
        except Exception as e:
            logger.error(f"Error generating signed URL for gs://{bucket_name}/{blob_name}: {e}. "
                         f"Check permissions (e.g., 'Service Account Token Creator' if using impersonation). "
                         f"Falling back to GCS URI.")
            return f"gs://{bucket_name}/{blob_name}"

    async def stream(self, prompt: str, session_id: str) -> AsyncIterable[dict[str, Any]]:
        """
        Handles streaming requests for video generation.
        Yields progress updates and the final video URL.
        `session_id` is the A2A Task ID, used here for logging and unique naming.
        """
        logger.info(f"VideoGenerationAgent stream started for session_id: {session_id}, prompt: '{prompt}'")

        
 

        yield {
            'is_task_complete': False,
            'updates': f"Received prompt: '{prompt}'. Starting VEO video generation.",
            'progress_percent': 0
        }

        start_time = time.monotonic()
        operation_kicked_off = False
        veo_operation_name_for_reporting = "N/A"
        try:
            logger.info(f"[{session_id}] Calling VEO with model: {self.VEO_MODEL_NAME}")
            veo_operation = await asyncio.to_thread(
                self.genai_client.models.generate_videos,
                model=self.VEO_MODEL_NAME,
                prompt=prompt,
                config=genai_types.GenerateVideosConfig(
                    person_generation=self.VEO_DEFAULT_PERSON_GENERATION,
                    aspect_ratio=self.VEO_DEFAULT_ASPECT_RATIO,
                ),
            )
            if hasattr(veo_operation, 'name') and veo_operation.name:
                veo_operation_name_for_reporting = veo_operation.name
            else:
                logger.warning(f"[{session_id}] Initial VEO operation object lacks a 'name' attribute or it's empty. Object: {str(veo_operation)[:200]}")
        

            operation_kicked_off = True
            logger.info(f"[{session_id}] VEO operation started: {veo_operation_name_for_reporting}")
            yield {
                'is_task_complete': False,
                'updates': f"VEO operation '{veo_operation_name_for_reporting}' started. Polling for completion...",
                'progress_percent': 5  # Small initial progress
            }

            while True:
                if not hasattr(veo_operation, 'done'):
                    error_msg = f"[{session_id}] VEO operation variable is not a valid operation object before 'done' check. Type: {type(veo_operation)}, Value: {str(veo_operation)[:200]}"
                    logger.error(error_msg)
                    raise TypeError(error_msg)

                if veo_operation.done:
                    break # Exit polling loop

                await asyncio.sleep(self.VEO_POLLING_INTERVAL_SECONDS)
                
                polled_data = await asyncio.to_thread(
                    self.genai_client.operations.get,
                    veo_operation
                )

                if hasattr(polled_data, 'done') and hasattr(polled_data, 'name'):
                    veo_operation = polled_data
                    if veo_operation.name:
                        veo_operation_name_for_reporting = veo_operation.name
                else:
                    error_msg = f"[{session_id}] VEO polling for '{veo_operation_name_for_reporting}' returned unexpected data type: {type(polled_data)}. Value: {str(polled_data)[:200]}"
                    logger.error(error_msg)
                    # Yield an error and exit stream, as we can't continue polling
                    yield {'is_task_complete': True, 'content': error_msg, 'final_message_text': "Video generation polling encountered an API issue.", 'progress_percent': 100}
                    return
                
                elapsed_time = time.monotonic() - start_time
                simulated_progress = min(int((elapsed_time / self.VEO_SIMULATED_TOTAL_GENERATION_TIME_SECONDS) * 100), 99)
                current_progress = max(5, simulated_progress)
                yield {
                    'is_task_complete': False,
                    'updates': f"Video generation in progress (Operation: {veo_operation_name_for_reporting}). Simulated progress: {current_progress}%",
                    'progress_percent': current_progress
                }

            logger.info(f"[{session_id}] VEO operation {veo_operation.name} is_done: {veo_operation.done}")

            if veo_operation.error:
                error_message_detail = getattr(veo_operation.error, 'message', str(veo_operation.error))
                error_message = f"VEO video generation failed: {error_message_detail}"
                logger.error(f"[{session_id}] {error_message} (Raw error: {veo_operation.error})")
                yield {
                    'is_task_complete': True,
                    'content': error_message,
                    'is_error': True,
                    'final_message_text': error_message,
                    'progress_percent': 100
                }
                return

            logger.debug(f"[{session_id}] VEO operation completed. Response: {str(veo_operation.response)[:500]}...") # Log truncated response
            
            if veo_operation.response and veo_operation.response.generated_videos:
                # Assuming we use the first generated video
                generated_video_info = veo_operation.response.generated_videos[0]
                video_obj = generated_video_info.video # Assumption: video_obj is always present

                gcs_uri_after_upload = None
                mime_type = "video/mp4"

                mime_type = video_obj.mime_type or mime_type
                logger.info(f"[{session_id}] Video object received. Bytes available: {video_obj.video_bytes is not None}, MimeType: {mime_type}")

                # Assumption: video_obj.video_bytes is always present and video_obj.uri is not used/None.
                if not video_obj.video_bytes:
                    logger.error(f"[{session_id}] Critical assumption violated: VEO response video_obj has no video_bytes.")
                    yield {
                        'is_task_complete': True,
                        'content': "VEO response video_obj has no video_bytes, cannot proceed.",
                        'is_error': True,
                        'final_message_text': "Video processing failed due to missing video byte data.",
                        'progress_percent': 100
                    }
                    return

                # Always upload the bytes to GCS
                logger.info(f"[{session_id}] Uploading video bytes from VEO to GCS bucket: {self.gcs_bucket_name}.")
                video_filename_base = f"veo_video_{session_id}_{uuid.uuid4()}"
                extension = mime_type.split('/')[-1] if '/' in mime_type else 'mp4'
                video_filename = f"{video_filename_base}.{extension}"
                gcs_blob_name = f"{session_id}/{video_filename}" # Organize by session_id in bucket
                gcs_uri_after_upload = await self._upload_bytes_to_gcs(
                    video_obj.video_bytes,
                    self.gcs_bucket_name,
                    gcs_blob_name,
                    mime_type
                )

                if gcs_uri_after_upload:
                    # Attempt to sign the GCS URI
                    blob_name_for_signing = gcs_uri_after_upload.replace(f"gs://{self.gcs_bucket_name}/", "")
                    signed_gcs_url = await self._generate_signed_url(
                        blob_name_for_signing,
                        self.gcs_bucket_name,
                        self.SIGNED_URL_EXPIRATION_SECONDS
                    )

                    video_filename_for_artifact = gcs_uri_after_upload.split("/")[-1] # Name from the uploaded GCS object
                    artifact_description = f"Generated video for prompt: '{prompt}'. Original GCS location: {gcs_uri_after_upload}"
                    completion_message = f"Video generation successful. Access video at link (expires): {signed_gcs_url}. Original GCS location: {gcs_uri_after_upload}"

                    if signed_gcs_url == gcs_uri_after_upload : # Signing failed or was not applicable, and it returned the original GCS URI
                         completion_message = f"Video generation successful. Video stored at GCS: {gcs_uri_after_upload}. A signed URL could not be generated."
                         logger.warning(f"[{session_id}] Signed URL generation might have failed or was not applicable, using GCS URI: {gcs_uri_after_upload}")

                    logger.info(f"[{session_id}] Yielding final success. Signed GCS URL: {signed_gcs_url}, Artifact Name: {video_filename_for_artifact}")
                    yield {
                        'is_task_complete': True,
                        'file_part_data': {
                            'uri': signed_gcs_url,
                            'mimeType': mime_type
                        },
                        'artifact_name': video_filename_for_artifact,
                        'artifact_description': artifact_description,
                        'final_message_text': completion_message,
                        'progress_percent': 100
                    }
                else:
                    err_message = "VEO generation completed, but failed to obtain a GCS URI after upload for signing."
                    logger.error(f"[{session_id}] {err_message} (gcs_uri_after_upload was None after attempting upload)")
                    yield {
                        'is_task_complete': True,
                        'content': err_message,
                        'is_error': True,
                        'final_message_text': err_message,
                        'progress_percent': 100
                    }
            
            elif hasattr(veo_operation.response, 'rai_media_filtered_count') and veo_operation.response.rai_media_filtered_count > 0:
                reasons = getattr(veo_operation.response, 'rai_media_filtered_reasons', ['Unknown safety filter.'])
                message = f"Video generation was blocked by safety filters. Reasons: {', '.join(str(r) for r in reasons)}"
                logger.warning(f"[{session_id}] {message}")
                yield {
                    'is_task_complete': True,
                    'content': message,
                    'is_error': True,
                    'final_message_text': message,
                    'progress_percent': 100
                }
            else:
                message = "VEO generation completed, but no video was returned in the response and no explicit safety filter indicated."
                logger.error(f"[{session_id}] {message} Full response: {str(veo_operation.response)[:500]}")
                yield {
                    'is_task_complete': True,
                    'content': message,
                    'is_error': True,
                    'final_message_text': message,
                    'progress_percent': 100
                }

        except Exception as e:
            error_context_msg = f"VEO operation name: {veo_operation_name_for_reporting}" if operation_kicked_off else "VEO operation not started."
            error_message = f"An error occurred during video generation stream for session_id {session_id}: {e}. Context: {error_context_msg}"
            logger.exception(error_message) # Log with traceback
            yield {
                'is_task_complete': True,
                'content': error_message,
                'is_error': True,
                'final_message_text': f"An unexpected error occurred: {e}",
                'progress_percent': 100
            }