A specialized agent for currency conversion built with AutoGen framework and the A2A Python SDK.

## Prerequisites

- Python 3.10 or higher
- `OPENAI_API_KEY` environment variable set
- A2A Python SDK (`a2a-sdk`)

## Setup

```bash
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
```

## Running

```bash
python -m agents.autogen
```

## Files

- `agents/autogen/__init__.py`: Package initialization
- `agents/autogen/__main__.py`: Entry point and server setup
- `agents/autogen/agent.py`: Core AutoGen agent logic
- `agents/autogen/agent_executor.py`: Adapter for A2A SDK
- `requirements.txt`: Dependencies
- `README.md`: Usage guide
