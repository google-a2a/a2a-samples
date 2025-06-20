import logging
import os

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
from agent_executor import ADKAgentExecutor
from dotenv import load_dotenv
from google.adk.agents import Agent
from google.adk.tools import google_search


load_dotenv()

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class MissingAPIKeyError(Exception):
    """Exception for missing API key."""


@click.command()
@click.option("--host", default="localhost")
@click.option("--port", default=10002)
def main(host, port):
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
        url="https://sample-a2a-agent-908687846511.us-central1.run.app/",
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
