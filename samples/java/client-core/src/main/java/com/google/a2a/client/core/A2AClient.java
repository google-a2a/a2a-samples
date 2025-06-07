package com.google.a2a.client.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.a2a.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A2A protocol client implementation based on A2A specification v0.2.1
 * Provides JSON-RPC and streaming communication functionality
 */
public class A2AClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, String> defaultHeaders;

    /**
     * Create a new A2A client
     *
     * @param baseUrl A2A server base URL
     */
    public A2AClient(String baseUrl) {
        this(baseUrl, HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build(), Map.of());
    }

    /**
     * Create A2A client with custom HTTP client and headers
     *
     * @param baseUrl A2A server base URL
     * @param httpClient custom HTTP client
     * @param defaultHeaders default headers to include in all requests (e.g., Authorization)
     */
    public A2AClient(String baseUrl, HttpClient httpClient, Map<String, String> defaultHeaders) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.defaultHeaders = defaultHeaders != null ? defaultHeaders : Map.of();
    }

    /**
     * Send message to agent using message/send method
     *
     * @param params message send parameters
     * @return Task or Message object depending on server response
     * @throws A2AClientException if the request fails
     */
    public Object sendMessage(MessageSendParams params) throws A2AClientException {
        JSONRPCRequest request = JSONRPCRequest.builder()
            .id(generateRequestId())
            .method("message/send")
            .params(convertToMap(params))
            .build();

        JSONRPCResponse response = doRequest(request);
        return convertResult(response.result());
    }

    /**
     * Send message and receive streaming response using message/stream method
     *
     * @param params message send parameters
     * @param listener event listener for streaming updates
     * @return CompletableFuture that completes when streaming ends
     */
    public CompletableFuture<Void> sendMessageStreaming(MessageSendParams params, StreamingEventListener listener) {
        return CompletableFuture.runAsync(() -> {
            try {
                JSONRPCRequest request = JSONRPCRequest.builder()
                    .id(generateRequestId())
                    .method("message/stream")
                    .params(convertToMap(params))
                    .build();

                String requestBody = objectMapper.writeValueAsString(request);

                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));

                // Add default headers (e.g., Authorization)
                defaultHeaders.forEach(requestBuilder::header);

                HttpRequest httpRequest = requestBuilder.build();

                HttpResponse<InputStream> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    String errorBody = new String(response.body().readAllBytes());
                    listener.onError(new A2AClientException("HTTP " + response.statusCode() + ": " + errorBody));
                    return;
                }

                // Parse Server-Sent Events
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                    String line;
                    StringBuilder eventData = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data:")) {
                            eventData.append(line.substring(5).trim());
                        } else if (line.isEmpty() && eventData.length() > 0) {
                            // Process complete event
                            try {
                                JsonNode eventNode = objectMapper.readTree(eventData.toString());
                                SendStreamingMessageResponse streamingResponse =
                                    objectMapper.treeToValue(eventNode, SendStreamingMessageResponse.class);

                                if (streamingResponse.error() != null) {
                                    JSONRPCError error = streamingResponse.error();
                                    Integer errorCode = error.code() != null ? error.code() : null;
                                    listener.onError(new A2AClientException(
                                        error.message(),
                                        errorCode
                                    ));
                                    return;
                                }

                                if (streamingResponse.result() != null) {
                                    // Convert the result to the appropriate object type
                                    Object deserializedEvent = deserializeStreamingEvent(streamingResponse.result());
                                    listener.onEvent(deserializedEvent);
                                }

                            } catch (Exception e) {
                                listener.onError(new A2AClientException("Failed to parse streaming response", e));
                                return;
                            }

                            eventData.setLength(0); // Reset for next event
                        }
                    }
                }

                listener.onComplete();

            } catch (Exception e) {
                listener.onError(new A2AClientException("Streaming request failed", e));
            }
        });
    }

    /**
     * Get task status using tasks/get method
     *
     * @param params task query parameters
     * @return Task object
     * @throws A2AClientException if the request fails
     */
    public Task getTask(TaskQueryParams params) throws A2AClientException {
        JSONRPCRequest request = JSONRPCRequest.builder()
            .id(generateRequestId())
            .method("tasks/get")
            .params(convertToMap(params))
            .build();

        JSONRPCResponse response = doRequest(request);
        return convertToTask(response.result());
    }

    /**
     * Cancel task using tasks/cancel method
     *
     * @param params task ID parameters
     * @return Task object
     * @throws A2AClientException if the request fails
     */
    public Task cancelTask(TaskIdParams params) throws A2AClientException {
        JSONRPCRequest request = JSONRPCRequest.builder()
            .id(generateRequestId())
            .method("tasks/cancel")
            .params(convertToMap(params))
            .build();

        JSONRPCResponse response = doRequest(request);
        return convertToTask(response.result());
    }

    /**
     * Set push notification configuration for a task
     *
     * @param config task push notification configuration
     * @return TaskPushNotificationConfig object confirming the configuration
     * @throws A2AClientException if the request fails
     */
    public TaskPushNotificationConfig setPushNotificationConfig(TaskPushNotificationConfig config) throws A2AClientException {
        JSONRPCRequest request = JSONRPCRequest.builder()
            .id(generateRequestId())
            .method("tasks/pushNotificationConfig/set")
            .params(convertToMap(config))
            .build();

        JSONRPCResponse response = doRequest(request);
        return convertToType(response.result(), TaskPushNotificationConfig.class);
    }

    /**
     * Get push notification configuration for a task
     *
     * @param params task ID parameters
     * @return TaskPushNotificationConfig object
     * @throws A2AClientException if the request fails
     */
    public TaskPushNotificationConfig getPushNotificationConfig(TaskIdParams params) throws A2AClientException {
        JSONRPCRequest request = JSONRPCRequest.builder()
            .id(generateRequestId())
            .method("tasks/pushNotificationConfig/get")
            .params(convertToMap(params))
            .build();

        JSONRPCResponse response = doRequest(request);
        return convertToType(response.result(), TaskPushNotificationConfig.class);
    }

    /**
     * Resubscribe to streaming updates for an existing task
     *
     * @param params task ID parameters
     * @param listener event listener for streaming updates
     * @return CompletableFuture that completes when streaming ends
     */
    public CompletableFuture<Void> resubscribeTask(TaskIdParams params, StreamingEventListener listener) {
        return CompletableFuture.runAsync(() -> {
            try {
                JSONRPCRequest request = JSONRPCRequest.builder()
                    .id(generateRequestId())
                    .method("tasks/resubscribe")
                    .params(convertToMap(params))
                    .build();

                String requestBody = objectMapper.writeValueAsString(request);

                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));

                // Add default headers (e.g., Authorization)
                defaultHeaders.forEach(requestBuilder::header);

                HttpRequest httpRequest = requestBuilder.build();

                HttpResponse<InputStream> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    String errorBody = new String(response.body().readAllBytes());
                    listener.onError(new A2AClientException("HTTP " + response.statusCode() + ": " + errorBody));
                    return;
                }

                // Parse Server-Sent Events (same logic as sendMessageStreaming)
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                    String line;
                    StringBuilder eventData = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            eventData.append(line.substring(6));
                        } else if (line.isEmpty() && eventData.length() > 0) {
                            try {
                                JsonNode eventNode = objectMapper.readTree(eventData.toString());
                                SendStreamingMessageResponse streamingResponse =
                                    objectMapper.treeToValue(eventNode, SendStreamingMessageResponse.class);

                                if (streamingResponse.error() != null) {
                                    JSONRPCError error = streamingResponse.error();
                                    Integer errorCode = error.code() != null ? error.code() : null;
                                    listener.onError(new A2AClientException(
                                        error.message(),
                                        errorCode
                                    ));
                                    return;
                                }

                                if (streamingResponse.result() != null) {
                                    // Convert the result to the appropriate object type
                                    Object deserializedEvent = deserializeStreamingEvent(streamingResponse.result());
                                    listener.onEvent(deserializedEvent);
                                }

                            } catch (Exception e) {
                                listener.onError(new A2AClientException("Failed to parse streaming response", e));
                                return;
                            }

                            eventData.setLength(0);
                        }
                    }
                }

                listener.onComplete();

            } catch (Exception e) {
                listener.onError(new A2AClientException("Resubscribe request failed", e));
            }
        });
    }

    /**
     * Get agent card information from well-known URI
     *
     * @return agent card
     * @throws A2AClientException if the request fails
     */
    public AgentCard getAgentCard() throws A2AClientException {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/.well-known/agent.json"))
                .header("Accept", "application/json")
                .GET();

            // Add default headers for authentication if needed
            defaultHeaders.forEach(requestBuilder::header);

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new A2AClientException("HTTP " + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), AgentCard.class);

        } catch (IOException | InterruptedException e) {
            throw new A2AClientException("Failed to get agent card", e);
        }
    }

    /**
     * Get authenticated extended agent card (requires authentication)
     *
     * @return extended agent card with additional details
     * @throws A2AClientException if the request fails
     */
    public AgentCard getAuthenticatedExtendedCard() throws A2AClientException {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/agent/authenticatedExtendedCard"))
                .header("Accept", "application/json")
                .GET();

            // Add default headers (authentication is required for this endpoint)
            defaultHeaders.forEach(requestBuilder::header);

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                throw new A2AClientException("Authentication required for extended card", 401);
            } else if (response.statusCode() == 403) {
                throw new A2AClientException("Access forbidden for extended card", 403);
            } else if (response.statusCode() != 200) {
                throw new A2AClientException("HTTP " + response.statusCode() + ": " + response.body());
            }

            return objectMapper.readValue(response.body(), AgentCard.class);

        } catch (IOException | InterruptedException e) {
            throw new A2AClientException("Failed to get authenticated extended card", e);
        }
    }

    /**
     * Convert object to Map for JSON-RPC params
     */
    private Map<String, Object> convertToMap(Object params) throws A2AClientException {
        try {
            String json = objectMapper.writeValueAsString(params);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new A2AClientException("Failed to convert params to map", e);
        }
    }

    /**
     * Convert response result to specific type
     */
    private <T> T convertToType(Object result, Class<T> clazz) throws A2AClientException {
        try {
            String json = objectMapper.writeValueAsString(result);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new A2AClientException("Failed to convert result to " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Convert response result to Task object
     */
    private Task convertToTask(Object result) throws A2AClientException {
        return convertToType(result, Task.class);
    }

    /**
     * Convert response result (could be Task, Message, or other types)
     */
    private Object convertResult(Object result) throws A2AClientException {
        try {
            String json = objectMapper.writeValueAsString(result);
            JsonNode node = objectMapper.readTree(json);

            // Check if it's a Task (has 'id' and 'status' fields)
            if (node.has("id") && node.has("status")) {
                return objectMapper.readValue(json, Task.class);
            }
            // Check if it's a Message (has 'role' and 'parts' fields)
            else if (node.has("role") && node.has("parts")) {
                return objectMapper.readValue(json, Message.class);
            }
            // Return as generic object if type cannot be determined
            else {
                return result;
            }
        } catch (Exception e) {
            throw new A2AClientException("Failed to convert result", e);
        }
    }

    /**
     * Execute JSON-RPC request
     */
    private JSONRPCResponse doRequest(JSONRPCRequest request) throws A2AClientException {
        try {
            String requestBody = objectMapper.writeValueAsString(request);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));

            // Add default headers (e.g., Authorization)
            defaultHeaders.forEach(requestBuilder::header);

            HttpRequest httpRequest = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new A2AClientException("HTTP " + response.statusCode() + ": " + response.body());
            }

            // First parse as JsonNode to check for errors
            JsonNode jsonNode = objectMapper.readTree(response.body());

            if (jsonNode.has("error")) {
                JsonNode errorNode = jsonNode.get("error");
                String message = errorNode.has("message") ? errorNode.get("message").asText() : "Unknown error";
                Integer code = errorNode.has("code") ? errorNode.get("code").asInt() : null;
                throw new A2AClientException(message, code);
            }

            return objectMapper.readValue(response.body(), JSONRPCResponse.class);

        } catch (IOException | InterruptedException e) {
            throw new A2AClientException("Request failed", e);
        }
    }

    /**
     * Generate request ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Deserialize streaming event based on its type
     */
    private Object deserializeStreamingEvent(Object event) throws A2AClientException {
        try {
            String json = objectMapper.writeValueAsString(event);
            JsonNode node = objectMapper.readTree(json);

            // Check if it's a TaskStatusUpdateEvent (has 'kind' field with 'status-update')
            if (node.has("kind") && "status-update".equals(node.get("kind").asText())) {
                return objectMapper.readValue(json, TaskStatusUpdateEvent.class);
            }
            // Check if it's a TaskArtifactUpdateEvent (has 'kind' field with 'artifact-update')
            else if (node.has("kind") && "artifact-update".equals(node.get("kind").asText())) {
                return objectMapper.readValue(json, TaskArtifactUpdateEvent.class);
            }
            // Check if it's a Task (has 'kind' field with 'task')
            else if (node.has("kind") && "task".equals(node.get("kind").asText())) {
                return objectMapper.readValue(json, Task.class);
            }
            // Check if it's a Message (has 'kind' field with 'message')
            else if (node.has("kind") && "message".equals(node.get("kind").asText())) {
                return objectMapper.readValue(json, Message.class);
            }
            // If we can't determine the type, return the original object
            else {
                return event;
            }
        } catch (Exception e) {
            throw new A2AClientException("Failed to deserialize streaming event", e);
        }
    }
}
