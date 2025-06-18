import asyncio
import logging
import os

import click
import uvicorn

from a2a.server.agent_execution import AgentExecutor, RequestContext
from a2a.server.apps import A2AStarletteApplication
from a2a.server.events import EventQueue
from a2a.server.request_handlers import DefaultRequestHandler
from a2a.server.tasks import InMemoryTaskStore, TaskUpdater
from a2a.types import (
    AgentCapabilities,
    AgentCard,
    AgentSkill,
    MessageSendParams,
    Part,
    TaskState,
    TextPart,
)
from a2a.utils import new_agent_text_message, new_task
from agent_executor import ADKAgentExecutor
from dotenv import load_dotenv
from google.adk.agents import Agent
from google.adk.artifacts import InMemoryArtifactService
from google.adk.memory.in_memory_memory_service import InMemoryMemoryService
from google.adk.runners import Runner
from google.adk.sessions import InMemorySessionService
from google.adk.tools import google_search
from google.genai import types


load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class MissingAPIKeyError(Exception):
    """Exception for missing API key."""


@click.command()
@click.option("--host", default="localhost")
@click.option("--port", default=10002)
def main(host, port):
    if not os.getenv("GOOGLE_API_KEY"):
        raise MissingAPIKeyError(
            "GOOGLE_API_KEY environment variable not set and GOOGLE_GENAI_USE_VERTEXAI is not TRUE."
        )

    facts_agent = Agent(
        name="facts_agent",
        model="gemini-2.5-flash-lite-preview-06-17",
        description=("Agent to give interesting facts."),
        instruction=("You are a helpful agent who can provide interesting facts."),
        tools=[google_search],
    )

    # Agent card (metadata)
    agent_card = AgentCard(
        name=facts_agent.name,
        description=facts_agent.description,
        url=f"http://{HOST}:{PORT}/",
        version="1.0.0",
        defaultInputModes=["text", "text/plain"],
        defaultOutputModes=["text", "text/plain"],
        capabilities=AgentCapabilities(streaming=True),
        skills=[
            AgentSkill(
                id="give_facts",
                name="Provide Interesting Facts",
                description="Searches Google for interesting facts",
                tags=["search", "google", "facts"],
                examples=[
                    "Provide an interesting fact about New York City.",
                ],
            )
        ],
    )

    request_handler = DefaultRequestHandler(
        agent_executor=ADKAgentExecutor(
            agent=facts_agent,
        ),
        task_store=InMemoryTaskStore(),
    )

    server = A2AStarletteApplication(
        agent_card=agent_card, http_handler=request_handler
    )

    uvicorn.run(server.build(), host=host, port=port)


if __name__ == "__main__":
    main()
