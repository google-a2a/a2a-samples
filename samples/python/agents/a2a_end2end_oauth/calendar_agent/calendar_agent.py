# Copyright 2025 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from datetime import datetime
import os, json, requests

from google.adk.agents import LlmAgent
from dotenv import load_dotenv
from google.adk import Agent
from google.adk.agents.callback_context import CallbackContext
from google.adk.tools.google_api_tool import CalendarToolset
from google.adk.tools import ToolContext

from google.oauth2.credentials import Credentials

# Load environment variables from .env file
load_dotenv()


def update_time(callback_context: CallbackContext):
  # get current date time
  now = datetime.now()
  formatted_time = now.strftime("%Y-%m-%d %H:%M:%S")
  callback_context.state["_time"] = formatted_time


def list_calendar_events(
    start_time: str,
    end_time: str,
    limit: int,
    tool_context: ToolContext,
) -> list[dict]:
  """Search for calendar events.

  Example:

      flights = get_calendar_events(
          calendar_id='joedoe@gmail.com',
          start_time='2024-09-17T06:00:00',
          end_time='2024-09-17T12:00:00',
          limit=10
      )
      # Returns up to 10 calendar events between 6:00 AM and 12:00 PM on
      September 17, 2024.

  Args:
      calendar_id (str): the calendar ID to search for events.
      start_time (str): The start of the time range (format is
        YYYY-MM-DDTHH:MM:SS).
      end_time (str): The end of the time range (format is YYYY-MM-DDTHH:MM:SS).
      limit (int): The maximum number of results to return.

  Returns:
      list[dict]: A list of events that match the search criteria.
  """
  # TODO: Instead of using the request module against a hard coded URL, should use the Google API module

  # Check if the tokes were already in the session state, which means the user
  # has already gone through the OAuth flow and successfully authenticated and
  # authorized the tool to access their calendar.
  print(f'calendar_agent: {tool_context.state}')
  if "calendar_access_token" in tool_context.state:
    print(f'calendar_agent: {tool_context.state["calendar_access_token"]}')
  
    # Example: List events from the primary calendar
    url = "https://www.googleapis.com/calendar/v3/calendars/primary/events"

    headers = {
        "Authorization": f"Bearer {tool_context.state["calendar_access_token"]}",
        "Content-Type": "application/json"
    }

    try:
        response = requests.get(
           url, 
           headers=headers, 
           params={
            "timeMin": start_time+'Z',
            "timeMax": end_time+'Z',
            "maxResults": limit
          }
        )
        response.raise_for_status()  # Raise an exception for bad status codes (4xx or 5xx)

        events = response.json()
        print(json.dumps(events, indent=2))

    except requests.exceptions.RequestException as e:
        print(f"Error: {e}")
    except json.JSONDecodeError as e:
        print(f"Error decoding JSON: {e}")

  return events


def create_calendar_agent() -> LlmAgent:
    """Constructs the ADK agent."""
    return LlmAgent(
      model="gemini-2.0-flash",
      name="calendar_agent",
      instruction="""
        You are a helpful personal calendar assistant.
        Use the provided tools to search for calendar events (use 10 as limit if user does't specify), and update them.
        Use "primary" as the calendarId if users don't specify.

        Scenario1:
        The user want to query the calendar events.
        Use list_calendar_events to search for calendar events.


        Current user:
        <User>
        {userInfo?}
        </User>
      """,
      tools=[
        list_calendar_events,
      ]
    )
