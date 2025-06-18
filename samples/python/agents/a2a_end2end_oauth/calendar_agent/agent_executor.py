import logging, json

from typing import TYPE_CHECKING

from datetime import datetime
import time

from a2a.server.agent_execution import AgentExecutor
from a2a.server.agent_execution.context import RequestContext
from a2a.server.events.event_queue import EventQueue
from a2a.server.tasks import TaskUpdater
from a2a.types import (
    AgentCard,
    FilePart,
    FileWithBytes,
    FileWithUri,
    Part,
    TaskState,
    TextPart,
    UnsupportedOperationError,
)
from a2a.utils.errors import ServerError
from google.adk import Runner
from google.genai import types
from google.adk.events import Event, EventActions


if TYPE_CHECKING:
    from google.adk.sessions.session import Session


logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

# Constants
DEFAULT_USER_ID = 'self'


class CalendarExecutor(AgentExecutor):
    """An AgentExecutor that runs an ADK-based Agent for calendar."""

    def __init__(self, runner: Runner, card: AgentCard):
        self.runner = runner
        self._card = card
        # Track active sessions for potential cancellation
        self._active_sessions: set[str] = set()

    async def _process_request(
        self,
        new_message: types.Content,
        session_id: str,
        access_token: str,
        task_updater: TaskUpdater,
    ) -> None:
        session_obj = await self._upsert_session(session_id)
        # Update session_id with the ID from the resolved session object.
        # (it may be the same as the one passed in if it already exists)
        session_id = session_obj.id

        logger.debug(f'Initial state: {session_obj.state}')

        # Track this session as active
        self._active_sessions.add(session_id)

        # Inject OAuth token into the session state
        state_changes={
            "calendar_access_token": access_token
        }
        actions_with_update = EventActions(state_delta=state_changes)
        current_time = time.time()
        system_event = Event(
            invocation_id="token_update",
            author="user", # Or 'agent', 'tool' etc.
            actions=actions_with_update,
            timestamp=current_time
            # content might be None or represent the action taken
        )
        await self.runner.session_service.append_event(session_obj, system_event)
        updated_session = await self.runner.session_service.get_session(
            app_name=self.runner.app_name,
            user_id=DEFAULT_USER_ID,
            session_id=session_id,
        )
        logger.debug(f'Updated state: {updated_session.state}')
        print("`append_event` called with explicit state delta.")

        try:
            async for event in self.runner.run_async(
                session_id=session_id,
                user_id=DEFAULT_USER_ID,
                new_message=new_message,
            ):
                # logger.debug('Event: %s', event)
                if event.is_final_response():
                    parts = [
                        convert_genai_part_to_a2a(part)
                        for part in event.content.parts
                        if (part.text or part.file_data or part.inline_data)
                    ]
                    logger.debug('Yielding final response: %s', parts)
                    await task_updater.add_artifact(parts)
                    await task_updater.update_status(
                        TaskState.completed, final=True
                    )
                    break
                if not event.get_function_calls():
                    logger.debug('Yielding update response')
                    await task_updater.update_status(
                        TaskState.working,
                        message=task_updater.new_agent_message(
                            [
                                convert_genai_part_to_a2a(part)
                                for part in event.content.parts
                                if (
                                    part.text
                                    or part.file_data
                                    or part.inline_data
                                )
                            ],
                        ),
                    )
                else:
                    logger.debug('Skipping event')
        finally:
            # Remove from active sessions when done
            self._active_sessions.discard(session_id)

    async def execute(
        self,
        context: RequestContext,
        event_queue: EventQueue,
    ):
        logger.debug(f'Request Call Context: {context.call_context}')
        logger.debug(f'Message parts in the Request Context: {context.message.parts}')
        # Run the agent until either complete or the task is suspended.
        updater = TaskUpdater(event_queue, context.task_id, context.context_id)
        # Immediately notify that the task is submitted.
        if not context.current_task:
            await updater.update_status(TaskState.submitted)
        await updater.update_status(TaskState.working)

        parts=[
            convert_a2a_part_to_genai(part)
            for part in context.message.parts
        ]
        logger.debug(f'parts from the conversion: {parts}')

        # Extract the OAuth Access Token from the request headers received by the A2A Server
        access_token=context.call_context.state['headers']['authorization'].split(' ')[1]
        logger.debug(f'access_token: {access_token}')

        await self._process_request(
            types.UserContent(
                parts=[
                    f'{convert_a2a_part_to_genai(part)} Time now is {datetime.now().strftime("%Y-%m-%d %H:%M:%S")}'
                    for part in context.message.parts
                ],
            ),
            context.context_id,
            access_token,
            updater,
        )
        logger.debug('[calendar] execute exiting')

    async def cancel(self, context: RequestContext, event_queue: EventQueue):
        """Cancel the execution for the given context.

        Currently logs the cancellation attempt as the underlying ADK runner
        doesn't support direct cancellation of ongoing tasks.
        """
        session_id = context.context_id
        if session_id in self._active_sessions:
            logger.info(
                f'Cancellation requested for active calendar session: {session_id}'
            )
            # TODO: Implement proper cancellation when ADK supports it
            self._active_sessions.discard(session_id)
        else:
            logger.debug(
                f'Cancellation requested for inactive calendar session: {session_id}'
            )

        raise ServerError(error=UnsupportedOperationError())

    async def _upsert_session(self, session_id: str) -> 'Session':
        """Retrieves a session if it exists, otherwise creates a new one.

        Ensures that async session service methods are properly awaited.
        """
        session = await self.runner.session_service.get_session(
            app_name=self.runner.app_name,
            user_id=DEFAULT_USER_ID,
            session_id=session_id,
        )
        if session is None:
            session = await self.runner.session_service.create_session(
                app_name=self.runner.app_name,
                user_id=DEFAULT_USER_ID,
                session_id=session_id,
            )
        return session


def convert_a2a_part_to_genai(part: Part) -> types.Part:
    """Convert a single A2A Part type into a Google Gen AI Part type.

    Args:
        part: The A2A Part to convert

    Returns:
        The equivalent Google Gen AI Part

    Raises:
        ValueError: If the part type is not supported
    """
    part = part.root
    if isinstance(part, TextPart):
        return types.Part(text=part.text)
    if isinstance(part, FilePart):
        if isinstance(part.file, FileWithUri):
            return types.Part(
                file_data=types.FileData(
                    file_uri=part.file.uri, mime_type=part.file.mime_type
                )
            )
        if isinstance(part.file, FileWithBytes):
            return types.Part(
                inline_data=types.Blob(
                    data=part.file.bytes, mime_type=part.file.mime_type
                )
            )
        raise ValueError(f'Unsupported file type: {type(part.file)}')
    raise ValueError(f'Unsupported part type: {type(part)}')


def convert_genai_part_to_a2a(part: types.Part) -> Part:
    """Convert a single Google Gen AI Part type into an A2A Part type.

    Args:
        part: The Google Gen AI Part to convert

    Returns:
        The equivalent A2A Part

    Raises:
        ValueError: If the part type is not supported
    """
    if part.text:
        return TextPart(text=part.text)
    if part.file_data:
        return FilePart(
            file=FileWithUri(
                uri=part.file_data.file_uri,
                mime_type=part.file_data.mime_type,
            )
        )
    if part.inline_data:
        return Part(
            root=FilePart(
                file=FileWithBytes(
                    bytes=part.inline_data.data,
                    mime_type=part.inline_data.mime_type,
                )
            )
        )
    raise ValueError(f'Unsupported part type: {part}')
