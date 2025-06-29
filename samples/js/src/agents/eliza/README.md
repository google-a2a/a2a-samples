# Eliza Agent

This agent uses the Eliza chatbot to provide Rogerian psychotherapy. To run:

    ```bash
    npm run agents:eliza
    ```

The agent server will start on `http://localhost:41241` and provides agent.json cards at three endpoints which are listed by the server.


## Chat with Eliza without Universal Authentication

The Eliza agent provides a well-known endpoint that does not require authentication.

1. Make sure Eliza is running:

    ```bash
    npm run agents:eliza
    ```

2. In a separate terminal window, use the standard command line interface to connect:

    ```bash
    npm run a2a:cli http://localhost:41241
    ```


## Chat with Eliza WITH Universal Authentication

Universal Authentication uses W3C Decentralized IDs (DIDs) and DID documents to scope agents to people, businesses, and governments.  Each DID document contains the cryptographic public keys which allow agents to authenticate without centralized authentication servers.

The Eliza agent provides the /agents/eliza endpoint that requires authentication.

1. Make sure you have created a demo agentic profile

    ```bash
    npm run agents:eliza:create-profile
    ```

2. Make sure Eliza is running:

    ```bash
    npm run agents:eliza
    ```

3. In a separate terminal window, use the special authenticating command line interface to connect:

    ```bash
    npm run agents:eliza:authcli http://localhost:41241/agents/eliza #connect
    ```

    Type in a message to the Eliza agent to cause an A2A RPC call to the server which triggers the authentication.

To read more about Universal Authentication and DIDs can be used with A2A please visit the [AgenticProfile blog](https://agenticprofile.ai)
