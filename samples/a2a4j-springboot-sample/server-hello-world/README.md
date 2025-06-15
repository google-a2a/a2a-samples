# A2A4J Server Hello World Sample

This is a complete A2A (Agent2Agent) protocol server implementation sample that demonstrates how to build a fully functional intelligent agent server using the A2A4J framework.

## Sample Features

- ✅ Complete A2A protocol implementation
- ✅ JSON-RPC 2.0 synchronous and streaming communication
- ✅ Automatic Agent Card discovery
- ✅ Multiple artifact type generation (text, code, summaries)
- ✅ Real-time status updates and progress tracking
- ✅ Server-Sent Events streaming responses
- ✅ CORS cross-origin support
- ✅ Detailed logging

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- curl or other HTTP client (for testing)

### Build Project

```bash
# Clone repository (if you haven't already)
git clone git@github.com:google-a2a/a2a-samples.git
cd a2a-samples/samples/a2a4j-springboot-samples/

# Build entire project
mvn clean install

# Navigate to server sample directory
cd server-hello-world
```

### Run Server

```bash
# Run with Maven
mvn spring-boot:run

# Or run compiled JAR
mvn clean package
java -jar target/server-hello-world-*.jar
```

The server will start at **http://localhost:8089**.

### Verify Server Status

```bash
# Check if server is running
curl -X GET http://localhost:8089/actuator/health

# Expected response
{"status":"UP"}
```

## A2A Protocol Endpoint Testing

### 1. Agent Card Discovery

Get agent capabilities and metadata information:

```bash
curl -X GET http://localhost:8089/.well-known/agent.json
```

**Expected Response Example:**
```json
{
  "id": "server-hello-world",
  "name": "A2A Java Server",
  "description": "A sample A2A agent implemented in Java",
  "url": "http://localhost:8089/a2a/server",
  "provider": {
    "organization": "A2A",
    "url": "https://github.com/google-a2a/a2a-samples"
  },
  "version": "1.0.0",
  "documentationUrl": "https://google-a2a.github.io/A2A/",
  "capabilities": {
    "streaming": true,
    "pushNotifications": false,
    "stateTransitionHistory": true
  },
  "defaultInputModes": [
    "text"
  ],
  "defaultOutputModes": [
    "text"
  ],
  "skills": [
    {
      "id": "hello-world",
      "name": "hello-world",
      "description": "A simple hello world skill",
      "tags": [
        "greeting",
        "basic"
      ],
      "examples": [
        "Say hello to me",
        "Greet me"
      ],
      "inputModes": [
        "text"
      ],
      "outputModes": [
        "text"
      ]
    }
  ]
}
```

### 2. Synchronous Message Sending

Send a message and wait for complete response:

```bash
curl -X POST http://localhost:8089/a2a/server \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "message/send",
    "params": {
      "message": {
        "role": "user",
        "parts": [
          {
            "kind": "text",
            "text": "Please help me analyze basic machine learning concepts"
          }
        ],
        "messageId": "9229e770-767c-417b-a0b0-f0741243c589"
      }
    },
    "id": "1"
  }'
```

**Expected Response Example:**
```json
{
    "jsonrpc": "2.0",
    "result": {
        "id": "ca1d01fd-0d55-455e-9de8-08b9a649799e",
        "contextId": "2afdf93f-2a7f-4c72-88b4-5a140f216ae5",
        "status": {
            "state": "completed",
            "message": {
                "role": "agent",
                "parts": [
                    {
                        "kind": "text",
                        "text": "Task completed successfully! I have generated a detailed response and example code for you."
                    }
                ],
                "kind": "message"
            },
            "timestamp": "1750007454357"
        },
        "artifacts": [
            {
                "artifactId": "text-response",
                "name": "AI Assistant Response",
                "description": "AI generated text reply",
                "parts": [
                    {
                        "kind": "text",
                        "text": "Here's my analysis of your question:\n\n"
                    },
                    {
                        "kind": "text",
                        "text": "Based on the information provided, I suggest the following approach:\n"
                    },
                    {
                        "kind": "text",
                        "text": "\n\nIf you have any questions, please feel free to ask!"
                    }
                ],
                "metadata": {
                    "chunkIndex": 1750007452641,
                    "contentType": "text/plain",
                    "encoding": "utf-8"
                }
            },
            {
                "artifactId": "code-example",
                "name": "Example Code",
                "description": "Example Java code generated based on requirements",
                "parts": [
                    {
                        "kind": "text",
                        "text": "// Example code\npublic class ExampleService {\n\n    public String processRequest(String input) {\n        if (input == null || input.trim().isEmpty()) {\n            return \"Input cannot be empty\";\n        }\n\n        // Process input\n        String processed = input.trim().toLowerCase();\n        return \"Processed result: \" + processed;\n    }\n}\n"
                    }
                ],
                "metadata": {
                    "language": "java",
                    "contentType": "text/x-java-source",
                    "filename": "ExampleService.java"
                }
            },
            {
                "artifactId": "task-summary",
                "name": "Task Summary",
                "description": "Summary report of this task execution",
                "parts": [
                    {
                        "kind": "text",
                        "text": "## Task Execution Summary\n\n✅ User request analysis completed\n✅ Text response generated\n✅ Example code provided\n✅ Task executed successfully\n\nTotal execution time: ~3 seconds\nGenerated content: Text response + Code example"
                    }
                ],
                "metadata": {
                    "contentType": "text/markdown",
                    "reportType": "summary"
                }
            }
        ],
        "history": [
            {
                "role": "agent",
                "parts": [
                    {
                        "kind": "text",
                        "text": "Starting to process user request..."
                    }
                ],
                "kind": "message"
            },
            {
                "role": "agent",
                "parts": [
                    {
                        "kind": "text",
                        "text": "Analyzing user input..."
                    }
                ],
                "kind": "message"
            },
            {
                "role": "agent",
                "parts": [
                    {
                        "kind": "text",
                        "text": "Generating response..."
                    }
                ],
                "kind": "message"
            },
            {
                "role": "agent",
                "parts": [
                    {
                        "kind": "text",
                        "text": "Task completed successfully! I have generated a detailed response and example code for you."
                    }
                ],
                "kind": "message"
            }
        ],
        "kind": "task"
    },
    "id": "1"
}
```

### 3. Streaming Message Sending

Send a message and receive real-time updates:

```bash
curl -X POST http://localhost:8089/a2a/server \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "jsonrpc": "2.0",
    "method": "message/stream",
    "params": {
      "message": {
        "role": "user",
        "parts": [
          {
            "kind": "text",
            "text": "Generate a simple Java class example"
          }
        ],
        "messageId": "9229e770-767c-417b-a0b0-f0741243c589"
      }
    },
    "id": "1"
  }'
```

**Expected Streaming Response:**
```
event:task-update
data:{"jsonrpc":"2.0","result":{"taskId":"d38eca97-d67f-4841-9bb0-88711d328901","contextId":"66e25835-c738-46db-8687-f5bcbe96b509","kind":"status-update","status":{"state":"working","message":{"role":"agent","parts":[{"kind":"text","kind":"text","text":"Starting to process user request..."}],"kind":"message"},"timestamp":"1750007467577"},"final":false,"metadata":null},"id":"1"}

event:task-update
data:{"jsonrpc":"2.0","result":{"taskId":"d38eca97-d67f-4841-9bb0-88711d328901","contextId":"66e25835-c738-46db-8687-f5bcbe96b509","kind":"status-update","status":{"state":"working","message":{"role":"agent","parts":[{"kind":"text","kind":"text","text":"Analyzing user input..."}],"kind":"message"},"timestamp":"1750007467579"},"final":false,"metadata":null},"id":"1"}

event:task-update
data:{"jsonrpc":"2.0","result":{"taskId":"d38eca97-d67f-4841-9bb0-88711d328901","contextId":"66e25835-c738-46db-8687-f5bcbe96b509","kind":"status-update","status":{"state":"working","message":{"role":"agent","parts":[{"kind":"text","kind":"text","text":"Generating response..."}],"kind":"message"},"timestamp":"1750007467582"},"final":false,"metadata":null},"id":"1"}

event:task-update
data:{"jsonrpc":"2.0","result":{"taskId":"d38eca97-d67f-4841-9bb0-88711d328901","contextId":"66e25835-c738-46db-8687-f5bcbe96b509","kind":"artifact-update","artifact":{"artifactId":"text-response","name":"AI Assistant Response","description":"AI generated text reply","parts":[{"kind":"text","kind":"text","text":"Here's my analysis of your question:\n\n"}],"metadata":{"chunkIndex":1750007465864,"contentType":"text/plain","encoding":"utf-8"}},"final":false,"append":false,"lastChunk":false,"metadata":{"artifactType":"text"}},"id":"1"}

event:task-update
data:{"jsonrpc":"2.0","result":{"taskId":"d38eca97-d67f-4841-9bb0-88711d328901","contextId":"66e25835-c738-46db-8687-f5bcbe96b509","kind":"artifact-update","artifact":{"artifactId":"text-response","name":"AI Assistant Response","description":"AI generated text reply","parts":[{"kind":"text","kind":"text","text":"Based on the information provided, I suggest the following approach:\n"}],"metadata":{"chunkIndex":1750007466166,"contentType":"text/plain","encoding":"utf-8"}},"final":false,"append":true,"lastChunk":false,"metadata":{"artifactType":"text"}},"id":"1"}

event:task-update
data:{"jsonrpc":"2.0","result":{"taskId":"d38eca97-d67f-4841-9bb0-88711d328901","contextId":"66e25835-c738-46db-8687-f5bcbe96b509","kind":"artifact-update","artifact":{"artifactId":"code-example","name":"Example Code","description":"Example Java code generated based on requirements","parts":[{"kind":"text","kind":"text","text":"// Example code\npublic class ExampleService {\n\n    public String processRequest(String input) {\n        if (input == null || input.trim().isEmpty()) {\n            return \"Input cannot be empty\";\n        }\n\n        // Process input\n        String processed = input.trim().toLowerCase();\n        return \"Processed result: \" + processed;\n    }\n}\n"}],"metadata":{"language":"java","contentType":"text/x-java-source","filename":"ExampleService.java"}},"final":false,"append":false,"lastChunk":true,"metadata":{"artifactType":"code"}},"id":"1"}

event:task-update
data:{"jsonrpc":"2.0","result":{"taskId":"d38eca97-d67f-4841-9bb0-88711d328901","contextId":"66e25835-c738-46db-8687-f5bcbe96b509","kind":"artifact-update","artifact":{"artifactId":"text-response","name":"AI Assistant Response","description":"AI generated text reply","parts":[{"kind":"text","kind":"text","text":"\n\nIf you have any questions, please feel free to ask!"}],"metadata":{"chunkIndex":1750007467072,"contentType":"text/plain","encoding":"utf-8"}},"final":false,"append":true,"lastChunk":true,"metadata":{"artifactType":"text"}},"id":"1"}

event:task-update
data:{"jsonrpc":"2.0","result":{"taskId":"d38eca97-d67f-4841-9bb0-88711d328901","contextId":"66e25835-c738-46db-8687-f5bcbe96b509","kind":"artifact-update","artifact":{"artifactId":"task-summary","name":"Task Summary","description":"Summary report of this task execution","parts":[{"kind":"text","kind":"text","text":"## Task Execution Summary\n\n✅ User request analysis completed\n✅ Text response generated\n✅ Example code provided\n✅ Task executed successfully\n\nTotal execution time: ~3 seconds\nGenerated content: Text response + Code example"}],"metadata":{"contentType":"text/markdown","reportType":"summary"}},"final":false,"append":false,"lastChunk":true,"metadata":{"artifactType":"summary"}},"id":"1"}

event:task-update
data:{"jsonrpc":"2.0","result":{"taskId":"d38eca97-d67f-4841-9bb0-88711d328901","contextId":"66e25835-c738-46db-8687-f5bcbe96b509","kind":"status-update","status":{"state":"completed","message":{"role":"agent","parts":[{"kind":"text","kind":"text","text":"Task completed successfully! I have generated a detailed response and example code for you."}],"kind":"message"},"timestamp":"1750007467589"},"final":true,"metadata":{"executionTime":"3000ms","artifactsGenerated":4,"success":true}},"id":"1"}
```

## Advanced Testing Scenarios

### Test Streaming Response Handling

Use more sophisticated tools to observe streaming responses:

```bash
# Use httpie to observe streaming responses
echo '{
  "jsonrpc": "2.0",
  "method": "message/stream",
  "params": {
    "message": {
      "role": "user",
      "parts": [{"type": "text", "kind": "text", "text": "Create a data structure example"}]
    }
  },
  "id": "advanced-1"
}' | http POST localhost:8089/a2a/server \
  Content-Type:application/json \
  Accept:text/event-stream
```

### Concurrent Request Testing

Test the server's ability to handle multiple concurrent requests:

```bash
# Start multiple concurrent requests
for i in {1..5}; do
  curl -X POST http://localhost:8089/a2a/server \
    -H "Content-Type: application/json" \
    -H "Accept: text/event-stream" \
    -d "{
      \"jsonrpc\": \"2.0\",
      \"method\": \"message/stream\",
      \"params\": {
        \"message\": {
          \"role\": \"user\",
          \"parts\": [{\"type\": \"text\", \"kind\": \"text\", \"text\": \"Concurrent request $i\"}]
        }
      },
      \"id\": \"concurrent-$i\"
    }" &
done

# Wait for all requests to complete
wait
```

### Error Handling Testing

Test various error scenarios:

```bash
# Test invalid JSON-RPC method
curl -X POST http://localhost:8089/a2a/server \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "invalid/method",
    "params": {},
    "id": "error-1"
  }'

# Test invalid parameters
curl -X POST http://localhost:8089/a2a/server \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "message/send",
    "params": {
      "invalidParam": "value"
    },
    "id": "error-2"
  }'
```

## Code Structure Explanation

### Core Components

- **`A2AServerApplication`**: Spring Boot main application class, configures CORS and application startup
- **`A2AServerController`**: REST controller implementing A2A protocol endpoints
- **`DemoAgentExecutor`**: Sample agent executor demonstrating various event types and artifact generation

### Execution Flow

1. **Task Creation**: Receives `message/send` or `message/stream` request
2. **Status Updates**: Sends "Starting", "Analyzing", "Generating" statuses
3. **Content Generation**: Sends text response in chunks
4. **Artifact Creation**: Generates code examples and task summaries
5. **Task Completion**: Sends final completion status and closes event queue

### Configuration Options

Configure in `application.yml`:

```yaml
server:
  port: 8089  # Modify server port
  
a2a:
  server:
    id: "server-hello-world"
    name: "A2A Java Server"
    description: "A sample A2A agent implemented in Java"
    version: "1.0.0"
    url: "http://localhost:${server.port}/a2a/server"
    provider:
      name: "A2A"
      url: "https://github.com/google-a2a/a2a-samples"
    documentationUrl: "https://google-a2a.github.io/A2A/"
    capabilities:
      streaming: true
      pushNotifications: false
      stateTransitionHistory: true
    defaultInputModes:
      - "text"
    defaultOutputModes:
      - "text"
    skills:
      - name: "hello-world"
        description: "A simple hello world skill"
        tags:
          - "greeting"
          - "basic"
        examples:
          - "Say hello to me"
          - "Greet me"
        inputModes:
          - "text"
        outputModes:
          - "text"
```

## Troubleshooting

### Common Issues

1. **Port in use**: Modify `server.port` in `application.yml`
2. **Java version incompatible**: Ensure using Java 17 or higher
3. **Dependency issues**: Run `mvn clean install` to rebuild

### Debug Mode

Enable detailed logging:

```yaml
logging:
  level:
    io.github.a2ap: DEBUG
    org.springframework.web: DEBUG
```

### Performance Monitoring

Add Spring Boot Actuator endpoints:

```bash
# View application info
curl http://localhost:8089/actuator/info

# View health status
curl http://localhost:8089/actuator/health

# View metrics
curl http://localhost:8089/actuator/metrics
```

## Extension Development

### Custom Agent Executor

Create your own `AgentExecutor` implementation:

```java
@Component
public class MyCustomExecutor implements AgentExecutor {
    
    @Override
    public Mono<Void> execute(RequestContext context, EventQueue eventQueue) {
        // Implement custom logic
        return Mono.empty();
    }
    
    @Override
    public Mono<Void> cancel(String taskId) {
        // Implement cancellation logic
        return Mono.empty();
    }
}
```

### Add Custom Endpoints

Extend controller to support more functionality:

```java
@RestController
public class CustomController {
    
    @GetMapping("/custom/endpoint")
    public ResponseEntity<String> customEndpoint() {
        return ResponseEntity.ok("Custom response");
    }
}
```

## Production Deployment

### Docker Deployment

```dockerfile
FROM openjdk:17-jre-slim
COPY target/server-hello-world-*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## References

- [A2A4J Core Documentation](https://github.com/a2ap/a2a4j/tree/main/a2a4j-core)
- [Spring Boot Starter Documentation](https://github.com/a2ap/a2a4j/tree/main/a2a4j-spring-boot-starter/a2a4j-server-spring-boot-starter)
- [A2A Protocol Specification](https://google-a2a.github.io/A2A/specification/)
- [JSON-RPC 2.0 Specification](https://www.jsonrpc.org/specification)

## License

This project is licensed under the Apache License 2.0
