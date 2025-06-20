# ADK Agent

This sample uses the Agent Development Kit (ADK) to create a simple "Expense Reimbursement" agent that is hosted as an A2A server.

This agent takes text requests from the client and, if any details are missing, returns a webform for the client (or its user) to fill out. After the client fills out the form, the agent will complete the task.

## Prerequisites

- Python 3.10 or higher
- [UV](https://docs.astral.sh/uv/)
- Access to an LLM and API Key

## Running the Sample

1. Navigate to the samples directory:

    ```bash
    cd samples/python/agents/google_adk
    ```

2. Create an environment file with your API key:

   ```bash
   echo "GOOGLE_API_KEY=your_api_key_here" > .env
   ```

3. Run an agent:

    ```bash
    uv run .
    ```

4. In a separate terminal, run the A2A client:

    ```sh
    # Connect to the agent (specify the agent URL with correct port)
    cd samples/python/hosts/cli
    uv run . --agent http://localhost:10002

    # If you changed the port when starting the agent, use that port instead
    # uv run . --agent http://localhost:YOUR_PORT
    ```

## Deploy to Google Cloud Run

```sh
gcloud run deploy sample-a2a-agent \
    --port=8080 \
    --source=. \
    --allow-unauthenticated \
    --region="us-central1" \
    --project=$GOOGLE_CLOUD_PROJECT \
    --set-env-vars=GOOGLE_CLOUD_PROJECT=$GOOGLE_CLOUD_PROJECT,GOOGLE_CLOUD_REGION=$GOOGLE_CLOUD_REGION,GOOGLE_GENAI_USE_VERTEXAI=true
```
