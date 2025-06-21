import logging, json
import os
from typing import Any
from a2a.types import AgentCard
from starlette.applications import Starlette
from starlette.responses import JSONResponse, PlainTextResponse
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.requests import Request

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)


class OAuth2Middleware(BaseHTTPMiddleware):
    """Starlette middleware that authenticates A2A access using an OAuth2 bearer token."""
    def __init__(self, app: Starlette, agent_card: AgentCard = None, public_paths: list[str] = None):
        super().__init__(app)
        self.agent_card = agent_card
        self.public_paths = set(public_paths or [])

        # Use app state for this demonstration (simplicity)
        self.a2a_auth = {}

    
    async def dispatch(self, request: Request, call_next):
        path = request.url.path

        # TODO: Additional token validation and authorization to be done here
        auth_header = request.headers.get('Authorization')
        if not auth_header:
            logger.debug(f'No access token yet')
            return await call_next(request)

        access_token = auth_header.split('Bearer ')[1]

        try:
            logger.debug(f'access token received: {access_token}')

        except Exception as e:
            return self._forbidden(f'Authentication failed: {e}', request)

        # Allow public paths and anonymous access
        if path in self.public_paths or not self.a2a_auth:
            return await call_next(request)

        return await call_next(request)
    
    
    def _forbidden(self, reason: str, request: Request):
        accept_header = request.headers.get('accept', '')
        if 'text/event-stream' in accept_header:
            return PlainTextResponse(f'error forbidden: {reason}', status_code=403, media_type='text/event-stream')
        return JSONResponse({ 'error': 'forbidden', 'reason': reason }, status_code=403)

    
    def _unauthorized(self, reason: str, request: Request):
        accept_header = request.headers.get('accept', '')
        if 'text/event-stream' in accept_header:
            return PlainTextResponse(f'error unauthorized: {reason}', status_code=401, media_type='text/event-stream')
        return JSONResponse({ 'error': 'unauthorized', 'reason': reason }, status_code=401)