import logging
import os

import click
from dotenv import load_dotenv

from a2a.server.apps import A2AStarletteApplication
from a2a.server.request_handlers import DefaultRequestHandler
from a2a.server.tasks import InMemoryTaskStore
from a2a.types import (
    AgentCapabilities,
    AgentCard,
    AgentSkill,
)
# Updated imports
from agent import VideoGenerationAgent 
from agent_executor import VideoGenerationAgentExecutor


load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class ConfigError(Exception):
    """Exception for configuration errors."""
    pass


@click.command()
@click.option('--host', default='localhost')
@click.option('--port', default=8001) # Changed port
def main(host, port):
    try:
        # Basic configuration checks (more detailed checks are in the Agent itself)
        use_vertex = os.getenv('GOOGLE_GENAI_USE_VERTEXAI', 'FALSE').upper() == 'TRUE'
        if not use_vertex and not os.getenv('GOOGLE_API_KEY'):
            raise ConfigError('GOOGLE_API_KEY must be set if GOOGLE_GENAI_USE_VERTEXAI is FALSE.')
        if use_vertex and (not os.getenv('VERTEX_AI_PROJECT_ID') or not os.getenv('VERTEX_AI_LOCATION')):
            raise ConfigError('VERTEX_AI_PROJECT_ID and VERTEX_AI_LOCATION must be set if GOOGLE_GENAI_USE_VERTEXAI is TRUE.')
        
        # Check for GCS bucket if not using Vertex AI and relying on GCS upload (optional, agent handles this)
        # if not use_vertex and not os.getenv('VIDEO_GEN_GCS_BUCKET'):
        # logger.warning("VIDEO_GEN_GCS_BUCKET is not set. Video upload might fail if VEO doesn't return a direct GCS URI.")


        capabilities = AgentCapabilities(streaming=True)
        skill = AgentSkill(
            id='generate_video_from_text_prompt',
            name='Generate Video from Text Prompt',
            description='Generates a short video clip based on a textual prompt using Google VEO.',
            tags=['video-generation', 'creative', 'multimedia', 'generative-ai', 'veo'],
            examples=[
                'Create a video of a futuristic cityscape at sunset.',
                'Generate a short clip of a cat playing with a yarn ball, cinematic style.',
                'Make a video: a serene beach with waves gently lapping the shore.'
            ],
        )
        agent_card = AgentCard(
            name='VEO Video Generation Agent',
            description='This agent generates short video clips from your text prompts using Google VEO and ADK.',
            url=f'http://{host}:{port}/',
            version='0.1.0',
            # As per A2A spec, these are lists of content types (strings)
            defaultInputModes=VideoGenerationAgent.SUPPORTED_CONTENT_TYPES, 
            defaultOutputModes=VideoGenerationAgent.SUPPORTED_OUTPUT_CONTENT_TYPES,
            capabilities=capabilities,
            skills=[skill],
        )
        request_handler = DefaultRequestHandler(
            agent_executor=VideoGenerationAgentExecutor(),
            task_store=InMemoryTaskStore(),
        )
        server = A2AStarletteApplication(
            agent_card=agent_card, http_handler=request_handler
        )
        import uvicorn
        logger.info(f"Starting VEO Video Generation Agent server on http://{host}:{port}")
        uvicorn.run(server.build(), host=host, port=port)
    except ConfigError as e:
        logger.error(f'Configuration Error: {e}')
        exit(1)
    except Exception as e:
        logger.error(f'An error occurred during server startup: {e}', exc_info=True)
        exit(1)


if __name__ == '__main__':
    main()
