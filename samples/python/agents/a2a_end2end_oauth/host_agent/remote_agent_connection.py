"""
Copyright 2025 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

from collections.abc import Callable
import google.oauth2.credentials
import google_auth_oauthlib.flow

import httpx, os

from a2a.client import A2AClient
from a2a.types import (
    AgentCard,
    SendMessageRequest,
    SendMessageResponse,
    Task,
    TaskArtifactUpdateEvent,
    TaskStatusUpdateEvent,
)
from dotenv import load_dotenv


load_dotenv()
access_token = None

TaskCallbackArg = Task | TaskStatusUpdateEvent | TaskArtifactUpdateEvent
TaskUpdateCallback = Callable[[TaskCallbackArg, AgentCard], Task]

class AgentAuth(httpx.Auth):
    """Custom httpx's authentication class to inject access token required by agent."""
    def __init__(self, agent_card: AgentCard):
        self.agent_card = agent_card

    def auth_flow(self, request):
        global access_token
        # auth = self.agent_card.authentication
        if not self.agent_card.securitySchemes:
            yield request
            return

        auth = self.agent_card.securitySchemes['google']

        # skip if not using oauth2 or credentials details are missing
        print(f'Agent Card Auth Config: {auth}')
        print(f'Agent Card Auth Type: {auth.root.type.lower()}')
        if not auth and not auth.root.type.lower() == 'oauth2':
            yield request
            return
        
        # TODO: The client config should not be hard-coded like this. Instead, it should respect the securitySchemes in the Agent Card
        # Instruction of how to setup the Google Cloud OAuth: https://developers.google.com/identity/protocols/oauth2
        if not access_token:
            flow = google_auth_oauthlib.flow.InstalledAppFlow.from_client_config(
                {
                    "web": {
                        "client_id": os.getenv('OAUTH_CLIENT_ID'),
                        "project_id": "myfirstargolisproject-332510",
                        "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                        "token_uri": "https://oauth2.googleapis.com/token",
                        "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                        "client_secret": os.getenv('OAUTH_CLIENT_SECRET'),
                        "redirect_uris": [
                            "http://localhost:10010/"
                        ]
                    }
                },
                scopes=[
                    "https://www.googleapis.com/auth/calendar",
                    "https://www.googleapis.com/auth/calendar.events",
                ]
            )

            # Make sure the host name and port matches with the redirect URL set in the Google Cloud Console
            credentials = flow.run_local_server(port=10010)
            access_token = credentials.token
            print(f'access token: {access_token}')
            print('Done.\n')
        
        request.headers['Authorization'] = f'Bearer {access_token}'
        yield request


class RemoteAgentConnections:
    """A class to hold the connections to the remote agents."""

    def __init__(self, agent_card: AgentCard, agent_url: str):
        print('====')
        print(f'agent_card: {agent_card}')
        print(f'agent_url: {agent_url}')

        self._httpx_client = httpx.AsyncClient(timeout=30)
        self._httpx_client.auth = AgentAuth(agent_card)
        self.agent_client = A2AClient(
            self._httpx_client, agent_card, url=agent_url
        )
        self.card = agent_card

    def get_agent(self) -> AgentCard:
        return self.card

    async def send_message(
        self, message_request: SendMessageRequest
    ) -> SendMessageResponse:
        print('send request', message_request.model_dump_json(exclude_none=True, indent=2))
        return await self.agent_client.send_message(message_request)
