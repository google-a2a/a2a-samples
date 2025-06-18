import logging, os, json

import click
import uvicorn

from a2a.server.apps import A2AStarletteApplication
from a2a.server.request_handlers import DefaultRequestHandler
from a2a.server.tasks import InMemoryTaskStore
from a2a.types import (
    AgentCapabilities,
    AgentCard,
    AgentSkill,
)
from calendar_agent import (
    create_calendar_agent,
)
from agent_executor import (
    CalendarExecutor,
)
from dotenv import load_dotenv
from google.adk.artifacts import InMemoryArtifactService
from google.adk.memory.in_memory_memory_service import InMemoryMemoryService
from google.adk.runners import Runner
from google.adk.sessions import InMemorySessionService

from oauth2_middleware import OAuth2Middleware


load_dotenv()

logging.basicConfig()

DEFAULT_HOST = 'localhost'
DEFAULT_PORT = 10001


def main(host: str = DEFAULT_HOST, port: int = DEFAULT_PORT):
    # Verify an API key is set.
    # Not required if using Vertex AI APIs.
    if os.getenv('GOOGLE_GENAI_USE_VERTEXAI') != 'TRUE' and not os.getenv(
        'GOOGLE_API_KEY'
    ):
        raise ValueError(
            'GOOGLE_API_KEY environment variable not set and '
            'GOOGLE_GENAI_USE_VERTEXAI is not TRUE.'
        )

    skill = AgentSkill(
        id='calendar_events_retrieval',
        name='Calendar events retrieval',
        description='Helps with calendar events retrieval',
        tags=['calendar'],
        examples=['Any events tomorrow?'],
    )

    agent_card = AgentCard(
        name='Calendar Agent',
        description='Helps with calendar events',
        url=f'http://{host}:{port}/',
        version='1.0.0',
        defaultInputModes=['text'],
        defaultOutputModes=['text'],
        capabilities=AgentCapabilities(streaming=True),
        skills=[skill],
        securitySchemes={
            "google": {
                "type": "oauth2",
                "flows": {
                    "authorizationCode": {
                        "authorizationUrl": "https://accounts.google.com/o/oauth2/auth",
                        "tokenUrl": "https://oauth2.googleapis.com/token",
                        "scopes": {
                            "https://www.googleapis.com/auth/calendar": "See, edit and delete Google Calendar"
                        }
                    }
                }
            }
        }
    )

    adk_agent = create_calendar_agent()
    runner = Runner(
        app_name=agent_card.name,
        agent=adk_agent,
        artifact_service=InMemoryArtifactService(),
        session_service=InMemorySessionService(),
        memory_service=InMemoryMemoryService(),
    )
    agent_executor = CalendarExecutor(runner, agent_card)

    request_handler = DefaultRequestHandler(
        agent_executor=agent_executor, task_store=InMemoryTaskStore()
    )

    server = A2AStarletteApplication(
        agent_card=agent_card, http_handler=request_handler
    )

    # Adding the middleware to inspect the OAuth token, actual authorization is not done in this demo
    app = server.build()
    app.add_middleware(OAuth2Middleware)

    # await uvicorn.Server(uvicorn.Config(app=app, host=host, port=port)).serve()
    uvicorn.run(app, host=host, port=port)


@click.command()
@click.option('--host', 'host', default=DEFAULT_HOST)
@click.option('--port', 'port', default=DEFAULT_PORT)
def cli(host: str, port: int):
    main(host, port)


if __name__ == '__main__':
    main()
