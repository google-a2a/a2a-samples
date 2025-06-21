# A2A End-to-End OAuth2 Demo
----
> *⚠️ DISCLAIMER: THIS DEMO IS INTENDED FOR DEMONSTRATION PURPOSES ONLY. IT IS NOT INTENDED FOR USE IN A PRODUCTION ENVIRONMENT.*  

> *⚠️ Important: A2A is a work in progress (WIP) thus, in the near future there might be changes that are different from what demonstrated here.*
----

The demo shows how an end-to-end OAuth flow can be done in the A2A framework.

It is developed on top of [this A2A sample](https://github.com/google-a2a/a2a-samples/tree/main/samples/python/agents/airbnb_planner_multiagent)

It shows a typical authorization flow of:
Human -OAuth Token-> Routing Agent -A2A-> Guest Agent -> Tool authenticated by an OAuth Token

In this way, the end-to-end authentication and authorization can be maintained across agents talking over the A2A protocol.

Notice that the Calendar Agent is the only flow with the end-to-end OAuth feature to access the Google Calendar API. Check the comments to see what are changed.

## Setup and Deployment

### Prerequisites

Before running the application locally, ensure you have the following installed:

1. **Node.js:** Required to run the Airbnb MCP server (if testing its functionality locally).
2. **uv:** The Python package management tool used in this project. Follow the installation guide: [https://docs.astral.sh/uv/getting-started/installation/](https://docs.astral.sh/uv/getting-started/installation/)
3. **python 3.13** Python 3.13 is required to run a2a-sdk 
4. **set up .env** 

- create .env file in `airbnb_agent`, `calendar_agent`, `host_agent`, `quote_agent`, `weather_agent` folder with reference to the individual .env.example file


## 1. Run Airbnb Agent

Run the airbnb agent server:

```bash
cd airbnb_agent
uv run .
```

## 2. Run Weather Agent
Open a new terminal and run the weather agent server:

```bash
cd weather_agent
uv run .
```

## 3. Run Calendar Agent
Open a new terminal and run the weather agent server:

```bash
cd calendar_agent
uv run .
```

## 4. Run Quote Agent
Open a new terminal and run the weather agent server:

```bash
cd quote_agent
uv run .
```

## 3. Run Host Agent
Open a new terminal and run the host agent server

```bash
cd host_agent
uv run .
```


## 4. Test at the UI

Here are example questions:

- "Tell me about weather in LA, CA"  

- "Please find a room in LA, CA, June 20-25, 2025, two adults"

- "Any events today?"

## References
- https://github.com/google/a2a-python
- https://codelabs.developers.google.com/intro-a2a-purchasing-concierge#1
- https://google.github.io/adk-docs/