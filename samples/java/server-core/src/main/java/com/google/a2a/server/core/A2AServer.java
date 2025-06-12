package com.google.a2a.server.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.a2a.model.AgentCard;
import com.google.a2a.model.Artifact;
import com.google.a2a.model.DataPart;
import com.google.a2a.model.FilePart;
import com.google.a2a.model.FileWithBytes;
import com.google.a2a.model.FileWithUri;
import com.google.a2a.model.JSONRPCError;
import com.google.a2a.model.JSONRPCRequest;
import com.google.a2a.model.JSONRPCResponse;
import com.google.a2a.model.Message;
import com.google.a2a.model.MessageSendParams;
import com.google.a2a.model.Part;
import com.google.a2a.model.PushNotificationAuthenticationInfo;
import com.google.a2a.model.PushNotificationConfig;
import com.google.a2a.model.SendStreamingMessageResponse;
import com.google.a2a.model.Task;
import com.google.a2a.model.TaskArtifactUpdateEvent;
import com.google.a2a.model.TaskIdParams;
import com.google.a2a.model.TaskPushNotificationConfig;
import com.google.a2a.model.TaskQueryParams;
import com.google.a2a.model.TaskState;
import com.google.a2a.model.TaskStatus;
import com.google.a2a.model.TaskStatusUpdateEvent;
import com.google.a2a.model.TextPart;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A2A Server implementation based on A2A specification v0.2.1
 * Provides centralized task management, JSON-RPC handling, and streaming communication
 * Automatically handles task lifecycle, allowing MessageHandler to focus on business logic
 */
public class A2AServer {

    private final AgentCard agentCard;
    private final long requestTimeout;
    private final MessageHandler messageHandler;
    private final Map<String, Task> taskStore;
    private final Map<String, List<Message>> taskHistory;
    private final Map<String, TaskPushNotificationConfig> pushNotificationConfigs;
    private final Map<String, List<StreamingConnection>> streamingConnections;
    private final ObjectMapper objectMapper;

    public A2AServer(AgentCard agentCard, MessageHandler messageHandler, ObjectMapper objectMapper, long requestTimeout) {
        this.agentCard = agentCard;
        this.messageHandler = messageHandler;
        this.taskStore = new ConcurrentHashMap<>();
        this.taskHistory = new ConcurrentHashMap<>();
        this.pushNotificationConfigs = new ConcurrentHashMap<>();
        this.streamingConnections = new ConcurrentHashMap<>();
        this.objectMapper = objectMapper;
        this.requestTimeout = requestTimeout;
    }

    /**
     * Handle message/send request
     */
    public JSONRPCResponse handleMessageSend(JSONRPCRequest request) {
        try {
            MessageSendParams params = parseParams(request.params(), MessageSendParams.class);

            // Create task context for synchronous processing
            TaskContextImpl context = new TaskContextImpl(params, false, null, request.id());

            // Let MessageHandler process the business logic
            messageHandler.handleMessage(context);

            // Return the result (task or direct message)
            Object result = context.getFinalResult();
            return createSuccessResponse(request.id(), result);

        } catch (Exception e) {
            return createErrorResponse(request.id(), -32603, "Internal error: " + e.getMessage());
        }
    }

    /**
     * Handle message/stream request (for streaming responses)
     */
    public void handleMessageStream(JSONRPCRequest request, Consumer<Object> eventCallback, Runnable completeCallback) {
        try {
            MessageSendParams params = parseParams(request.params(), MessageSendParams.class);

            // Create streaming connection
            StreamingConnection connection = new StreamingConnection(request.id(), eventCallback, completeCallback);

            // Create task context for streaming processing
            TaskContextImpl context = new TaskContextImpl(params, true, connection, request.id());

            // Let MessageHandler process the business logic
            messageHandler.handleMessage(context);

            // Auto-complete if not already completed
            context.autoComplete();

        } catch (Exception e) {
            // Send error event
            JSONRPCError error = JSONRPCError.builder()
                    .code(-32603)
                    .message("Internal error: " + e.getMessage())
                    .build();

            SendStreamingMessageResponse errorResponse = SendStreamingMessageResponse.builder()
                    .id(request.id())
                    .jsonrpc("2.0")
                    .error(error)
                    .build();

            eventCallback.accept(errorResponse);
            completeCallback.run();
        }
    }

    /**
     * Handle tasks/get request
     */
    public JSONRPCResponse handleTaskGet(JSONRPCRequest request) {
        try {
            TaskQueryParams params = parseParams(request.params(), TaskQueryParams.class);

            Task task = taskStore.get(params.id());
            if (task == null) {
                return createErrorResponse(request.id(), -32001, "Task not found");
            }

            // Include history if requested
            if (params.historyLength() != null && params.historyLength() > 0) {
                List<Message> history = getTaskHistory(params.id());
                int limit = Math.min(params.historyLength(), history.size());
                List<Message> limitedHistory = history.subList(Math.max(0, history.size() - limit), history.size());

                // Create task with history
                Task taskWithHistory = Task.builder()
                        .id(task.id())
                        .contextId(task.contextId())
                        .status(task.status())
                        .artifacts(task.artifacts())
                        .history(limitedHistory)
                        .metadata(task.metadata())
                        .build();

                return createSuccessResponse(request.id(), taskWithHistory);
            }

            return createSuccessResponse(request.id(), task);

        } catch (Exception e) {
            return createErrorResponse(request.id(), -32602, "Invalid parameters: " + e.getMessage());
        }
    }

    /**
     * Handle tasks/cancel request
     */
    public JSONRPCResponse handleTaskCancel(JSONRPCRequest request) {
        try {
            TaskIdParams params = parseParams(request.params(), TaskIdParams.class);

            Task task = taskStore.get(params.id());
            if (task == null) {
                return createErrorResponse(request.id(), -32001, "Task not found");
            }

            // Check if task can be canceled
            TaskState currentState = task.status().state();
            if (currentState == TaskState.COMPLETED ||
                    currentState == TaskState.CANCELED ||
                    currentState == TaskState.FAILED ||
                    currentState == TaskState.REJECTED) {
                return createErrorResponse(request.id(), -32002, "Task cannot be canceled");
            }

            // Create canceled status with timestamp
            TaskStatus canceledStatus = TaskStatus.builder()
                    .state(TaskState.CANCELED)
                    .timestamp(Instant.now().toString())
                    .build();

            // Update task status to canceled
            Task canceledTask = Task.builder()
                    .id(task.id())
                    .contextId(task.contextId())
                    .status(canceledStatus)
                    .artifacts(task.artifacts())
                    .history(task.history())
                    .metadata(task.metadata())
                    .build();

            // Update task and notify listeners
            updateTaskInternal(canceledTask, true);

            return createSuccessResponse(request.id(), canceledTask);

        } catch (Exception e) {
            return createErrorResponse(request.id(), -32602, "Invalid parameters: " + e.getMessage());
        }
    }

    /**
     * Handle tasks/pushNotificationConfig/set request
     */
    public JSONRPCResponse handleSetPushNotificationConfig(JSONRPCRequest request) {
        try {
            if (!agentCard.capabilities().pushNotifications()) {
                return createErrorResponse(request.id(), -32003, "Push notifications not supported");
            }

            TaskPushNotificationConfig config = parseParams(request.params(), TaskPushNotificationConfig.class);

            // Validate that the task exists
            if (!taskStore.containsKey(config.taskId())) {
                return createErrorResponse(request.id(), -32001, "Task not found");
            }

            // Store the configuration
            pushNotificationConfigs.put(config.taskId(), config);

            // Return the configuration (potentially masking sensitive data)
            TaskPushNotificationConfig response = TaskPushNotificationConfig.builder()
                    .taskId(config.taskId())
                    .pushNotificationConfig(PushNotificationConfig.builder()
                            .url(config.pushNotificationConfig().url())
                            .token(config.pushNotificationConfig().token())
                            // Don't return sensitive authentication details
                            .authentication(config.pushNotificationConfig().authentication() != null ?
                                    PushNotificationAuthenticationInfo.builder()
                                            .schemes(config.pushNotificationConfig().authentication().schemes())
                                            .credentials("***masked***")
                                            .build() : null)
                            .build())
                    .build();

            return createSuccessResponse(request.id(), response);

        } catch (Exception e) {
            return createErrorResponse(request.id(), -32602, "Invalid parameters: " + e.getMessage());
        }
    }

    /**
     * Handle tasks/pushNotificationConfig/get request
     */
    public JSONRPCResponse handleGetPushNotificationConfig(JSONRPCRequest request) {
        try {
            if (!agentCard.capabilities().pushNotifications()) {
                return createErrorResponse(request.id(), -32003, "Push notifications not supported");
            }

            TaskIdParams params = parseParams(request.params(), TaskIdParams.class);

            TaskPushNotificationConfig config = pushNotificationConfigs.get(params.id());
            if (config == null) {
                return createErrorResponse(request.id(), -32001, "Push notification config not found for task");
            }

            // Return the configuration (potentially masking sensitive data)
            TaskPushNotificationConfig response = TaskPushNotificationConfig.builder()
                    .taskId(config.taskId())
                    .pushNotificationConfig(PushNotificationConfig.builder()
                            .url(config.pushNotificationConfig().url())
                            .token(config.pushNotificationConfig().token())
                            // Don't return sensitive authentication details
                            .authentication(config.pushNotificationConfig().authentication() != null ?
                                    PushNotificationAuthenticationInfo.builder()
                                            .schemes(config.pushNotificationConfig().authentication().schemes())
                                            .credentials("***masked***")
                                            .build() : null)
                            .build())
                    .build();

            return createSuccessResponse(request.id(), response);

        } catch (Exception e) {
            return createErrorResponse(request.id(), -32602, "Invalid parameters: " + e.getMessage());
        }
    }

    /**
     * Handle tasks/resubscribe request (for reconnecting to streaming)
     */
    public void handleTaskResubscribe(JSONRPCRequest request, Consumer<Object> eventCallback, Runnable completeCallback) {
        try {
            if (!agentCard.capabilities().streaming()) {
                JSONRPCError error = JSONRPCError.builder()
                        .code(-32004)
                        .message("Streaming not supported")
                        .build();

                SendStreamingMessageResponse errorResponse = SendStreamingMessageResponse.builder()
                        .id(request.id())
                        .jsonrpc("2.0")
                        .error(error)
                        .build();

                eventCallback.accept(errorResponse);
                completeCallback.run();
                return;
            }

            TaskIdParams params = parseParams(request.params(), TaskIdParams.class);

            Task task = taskStore.get(params.id());
            if (task == null) {
                JSONRPCError error = JSONRPCError.builder()
                        .code(-32001)
                        .message("Task not found")
                        .build();

                SendStreamingMessageResponse errorResponse = SendStreamingMessageResponse.builder()
                        .id(request.id())
                        .jsonrpc("2.0")
                        .error(error)
                        .build();

                eventCallback.accept(errorResponse);
                completeCallback.run();
                return;
            }

            // Create streaming connection for this task
            StreamingConnection connection = new StreamingConnection(request.id(), eventCallback, completeCallback);
            addStreamingConnection(params.id(), connection);

            // Send current task state
            SendStreamingMessageResponse currentState = SendStreamingMessageResponse.builder()
                    .id(request.id())
                    .jsonrpc("2.0")
                    .result(task)
                    .build();

            eventCallback.accept(currentState);

        } catch (Exception e) {
            JSONRPCError error = JSONRPCError.builder()
                    .code(-32602)
                    .message("Invalid parameters: " + e.getMessage())
                    .build();

            SendStreamingMessageResponse errorResponse = SendStreamingMessageResponse.builder()
                    .id(request.id())
                    .jsonrpc("2.0")
                    .error(error)
                    .build();

            eventCallback.accept(errorResponse);
            completeCallback.run();
        }
    }

    /**
     * Get agent card information
     */
    public AgentCard getAgentCard() {
        return agentCard;
    }

    /**
     * Get authenticated extended agent card (if supported)
     * This would typically include additional skills or details not in the public card
     */
    public AgentCard getAuthenticatedExtendedCard() {
        // For this implementation, return the same card
        // In real implementations, this might include additional skills or details
        return agentCard;
    }

    /**
     * Get task history
     */
    public List<Message> getTaskHistory(String taskId) {
        return taskHistory.getOrDefault(taskId, List.of());
    }

    /**
     * Internal method to update task and notify all listeners
     */
    private void updateTaskInternal(Task task, boolean isFinalUpdate) {
        // Store the task
        taskStore.put(task.id(), task);

        // Create status update event
        TaskStatusUpdateEvent statusEvent = TaskStatusUpdateEvent.builder()
                .taskId(task.id())
                .contextId(task.contextId())
                .status(task.status())
                .finalEvent(isFinalUpdate)
                .build();

        // Notify all streaming connections for this task
        notifyStreamingConnections(task.id(), statusEvent);

        // Close connections if final update
        if (isFinalUpdate) {
            closeStreamingConnections(task.id());
        }
    }

    /**
     * Add a streaming connection for a task
     */
    private void addStreamingConnection(String taskId, StreamingConnection connection) {
        streamingConnections.computeIfAbsent(taskId, k -> new CopyOnWriteArrayList<>())
                .add(connection);
    }

    /**
     * Notify all streaming connections for a task
     */
    private void notifyStreamingConnections(String taskId, Object event) {
        List<StreamingConnection> connections = streamingConnections.get(taskId);
        streamingConnections.compute(taskId, (id, connectionsList) -> {
            if (connections != null) {
                connections.removeIf(connection -> !connection.sendEvent(event));
            }
            return connectionsList;
        });
    }

    /**
     * Close all streaming connections for a task
     */
    private void closeStreamingConnections(String taskId) {
        List<StreamingConnection> connections = streamingConnections.remove(taskId);
        if (connections != null) {
            connections.forEach(StreamingConnection::close);
        }
    }

    /**
     * Parse request parameters
     */
    private <T> T parseParams(Object params, Class<T> clazz) throws Exception {
        if (params instanceof Map) {
            String json = objectMapper.writeValueAsString(params);
            return objectMapper.readValue(json, clazz);
        } else if (params instanceof JsonNode) {
            return objectMapper.treeToValue((JsonNode) params, clazz);
        } else {
            return objectMapper.convertValue(params, clazz);
        }
    }

    /**
     * Create success response
     */
    private JSONRPCResponse createSuccessResponse(Object id, Object result) {
        return JSONRPCResponse.builder()
                .id(id)
                .jsonrpc("2.0")
                .result(result)
                .build();
    }

    /**
     * Create error response
     */
    private JSONRPCResponse createErrorResponse(Object id, int code, String message) {
        JSONRPCError error = JSONRPCError.builder()
                .code(code)
                .message(message)
                .build();

        return JSONRPCResponse.builder()
                .id(id)
                .jsonrpc("2.0")
                .error(error)
                .build();
    }

    /**
     * Internal implementation of TaskContext with simplified business logic interface
     */
    private class TaskContextImpl implements TaskContext {
        private final MessageSendParams params;
        private final boolean streaming;
        private final StreamingConnection streamingConnection;
        private final Object requestId;
        private Task currentTask;
        private String taskId;
        private String contextId;
        private Object finalResult;
        private boolean completed = false;
        private boolean taskCreated = false;

        public TaskContextImpl(MessageSendParams params, boolean streaming,
                               StreamingConnection streamingConnection, Object requestId) {
            this.params = params;
            this.streaming = streaming;
            this.streamingConnection = streamingConnection;
            this.requestId = requestId;
            this.contextId = params.message().contextId() != null ?
                    params.message().contextId() : UUID.randomUUID().toString();
        }

        @Override
        public void setWorking(String message) {
            ensureTaskCreated();
            updateTaskStatus(TaskState.WORKING, message);
        }

        @Override
        public void setInputRequired(String message) {
            ensureTaskCreated();
            updateTaskStatus(TaskState.INPUT_REQUIRED, message);
        }

        @Override
        public void setAuthRequired(String message) {
            ensureTaskCreated();
            updateTaskStatus(TaskState.AUTH_REQUIRED, message);
        }

        @Override
        public void setCompleted(String message) {
            ensureTaskCreated();
            updateTaskStatus(TaskState.COMPLETED, message);
            markCompleted();
        }

        @Override
        public void setFailed(String errorMessage) {
            ensureTaskCreated();
            updateTaskStatus(TaskState.FAILED, errorMessage);
            markCompleted();
        }

        @Override
        public void addTextArtifact(String name, String text, String description, boolean append, boolean lastChunk) {
            ensureTaskCreated();

            Artifact artifact = Artifact.builder()
                    .artifactId(generateArtifactId(name))
                    .name(name)
                    .description(description)
                    .parts(List.of(TextPart.builder().text(text).build()))
                    .build();

            addArtifactInternal(artifact, append, lastChunk);
        }

        @Override
        public void addTextArtifact(String name, String text) {
            addTextArtifact(name, text, null, false, true);
        }

        @Override
        public void addFileArtifact(String name, String fileName, String mimeType, String fileContent, boolean isUri, String description) {
            ensureTaskCreated();

            FilePart filePart;
            if (isUri) {
                filePart = FilePart.builder()
                        .file(FileWithUri.builder()
                                .name(fileName)
                                .mimeType(mimeType)
                                .uri(fileContent)
                                .build())
                        .build();
            } else {
                filePart = FilePart.builder()
                        .file(FileWithBytes.builder()
                                .name(fileName)
                                .mimeType(mimeType)
                                .bytes(fileContent)
                                .build())
                        .build();
            }

            Artifact artifact = Artifact.builder()
                    .artifactId(generateArtifactId(name))
                    .name(name)
                    .description(description)
                    .parts(List.of(filePart))
                    .build();

            addArtifactInternal(artifact, false, true);
        }

        @Override
        public void addDataArtifact(String name, Object data, String description) {
            ensureTaskCreated();

            // Convert Object to Map<String, Object> if needed
            Map<String, Object> dataMap;
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> castedMap = (Map<String, Object>) data;
                dataMap = castedMap;
            } else {
                dataMap = Map.of("data", data);
            }

            Artifact artifact = Artifact.builder()
                    .artifactId(generateArtifactId(name))
                    .name(name)
                    .description(description)
                    .parts(List.of(DataPart.builder().data(dataMap).build()))
                    .build();

            addArtifactInternal(artifact, false, true);
        }

        @Override
        public void sendDirectMessage(String text) {
            Message response = Message.builder()
                    .messageId(UUID.randomUUID().toString())
                    .role("agent")
                    .parts(List.of(TextPart.builder().text(text).build()))
                    .contextId(contextId)
                    .kind("message")
                    .build();

            sendDirectMessageInternal(response);
        }

        @Override
        public void sendDirectMessage(String text, Object data) {
            List<Part> parts = new ArrayList<>();
            parts.add(TextPart.builder().text(text).build());

            // Convert Object to Map<String, Object> if needed
            Map<String, Object> dataMap;
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> castedMap = (Map<String, Object>) data;
                dataMap = castedMap;
            } else {
                dataMap = Map.of("data", data);
            }

            parts.add(DataPart.builder().data(dataMap).build());

            Message response = Message.builder()
                    .messageId(UUID.randomUUID().toString())
                    .role("agent")
                    .parts(parts)
                    .contextId(contextId)
                    .kind("message")
                    .build();

            sendDirectMessageInternal(response);
        }

        @Override
        public Message getUserMessage() {
            return params.message();
        }

        @Override
        public String extractUserText() {
            if (params.message().parts() == null || params.message().parts().isEmpty()) {
                return null;
            }

            StringBuilder textBuilder = new StringBuilder();
            for (Part part : params.message().parts()) {
                if (part instanceof TextPart textPart) {
                    if (textBuilder.length() > 0) {
                        textBuilder.append("\n");
                    }
                    textBuilder.append(textPart.text());
                }
            }

            return textBuilder.toString();
        }

        @Override
        public List<FilePart> extractUserFiles() {
            if (params.message().parts() == null) {
                return List.of();
            }

            return params.message().parts().stream()
                    .filter(part -> part instanceof FilePart)
                    .map(part -> (FilePart) part)
                    .toList();
        }

        @Override
        public List<DataPart> extractUserData() {
            if (params.message().parts() == null) {
                return List.of();
            }

            return params.message().parts().stream()
                    .filter(part -> part instanceof DataPart)
                    .map(part -> (DataPart) part)
                    .toList();
        }

        @Override
        public boolean isStreaming() {
            return streaming;
        }

        @Override
        public String getTaskId() {
            return taskId;
        }

        @Override
        public String getContextId() {
            return contextId;
        }

        @Override
        public Map<String, Object> getMetadata() {
            return params.metadata() != null ? params.metadata() : Map.of();
        }

        // Internal methods

        public Object getFinalResult() {
            return finalResult;
        }

        public void autoComplete() {
            if (!completed && taskCreated) {
                // Auto-complete task if not already completed
                setCompleted(null);
            }
        }

        private void ensureTaskCreated() {
            if (!taskCreated) {
                createInitialTask();
            }
        }

        private void createInitialTask() {
            taskId = UUID.randomUUID().toString();

            TaskStatus initialStatus = TaskStatus.builder()
                    .state(TaskState.SUBMITTED)
                    .timestamp(Instant.now().toString())
                    .build();

            currentTask = Task.builder()
                    .id(taskId)
                    .contextId(contextId)
                    .status(initialStatus)
                    .artifacts(new ArrayList<>())
                    .metadata(params.metadata())
                    .build();

            // Store task and history
            taskStore.put(taskId, currentTask);
            taskHistory.computeIfAbsent(taskId, k -> new CopyOnWriteArrayList<>())
                    .add(params.message());

            // Add streaming connection if streaming
            if (streaming && streamingConnection != null) {
                addStreamingConnection(taskId, streamingConnection);
            }

            // Send initial task event if streaming
            if (streaming) {
                sendStreamingEvent(currentTask);
            } else {
                finalResult = currentTask;
            }

            taskCreated = true;
        }

        private void updateTaskStatus(TaskState state, String message) {
            TaskStatus.Builder statusBuilder = TaskStatus.builder()
                    .state(state)
                    .timestamp(Instant.now().toString());

            if (message != null && !message.trim().isEmpty()) {
                Message statusMessage = Message.builder()
                        .messageId(UUID.randomUUID().toString())
                        .role("agent")
                        .parts(List.of(TextPart.builder().text(message).build()))
                        .contextId(contextId)
                        .taskId(taskId)
                        .kind("message")
                        .build();
                statusBuilder.message(statusMessage);
            }

            TaskStatus status = statusBuilder.build();

            currentTask = Task.builder()
                    .id(currentTask.id())
                    .contextId(currentTask.contextId())
                    .status(status)
                    .artifacts(currentTask.artifacts())
                    .history(currentTask.history())
                    .metadata(currentTask.metadata())
                    .build();

            taskStore.put(taskId, currentTask);

            // Check if this is a terminal status
            boolean isFinal = isTerminalState(state);

            // Send status update event if streaming
            if (streaming) {
                TaskStatusUpdateEvent statusEvent = TaskStatusUpdateEvent.builder()
                        .taskId(taskId)
                        .contextId(contextId)
                        .status(status)
                        .finalEvent(isFinal)
                        .build();

                sendStreamingEvent(statusEvent);

                if (isFinal) {
                    markCompleted();
                }
            } else {
                finalResult = currentTask;
            }
        }

        private void addArtifactInternal(Artifact artifact, boolean append, boolean lastChunk) {
            List<Artifact> artifacts = currentTask.artifacts() != null ?
                    new ArrayList<>(currentTask.artifacts()) : new ArrayList<>();

            if (append && !artifacts.isEmpty()) {
                // Update existing artifact if same ID
                boolean updated = false;
                for (int i = 0; i < artifacts.size(); i++) {
                    if (artifacts.get(i).artifactId().equals(artifact.artifactId())) {
                        artifacts.set(i, artifact);
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    artifacts.add(artifact);
                }
            } else {
                artifacts.add(artifact);
            }

            currentTask = Task.builder()
                    .id(currentTask.id())
                    .contextId(currentTask.contextId())
                    .status(currentTask.status())
                    .artifacts(artifacts)
                    .history(currentTask.history())
                    .metadata(currentTask.metadata())
                    .build();

            taskStore.put(taskId, currentTask);

            // Send artifact update event if streaming
            if (streaming) {
                TaskArtifactUpdateEvent artifactEvent = TaskArtifactUpdateEvent.builder()
                        .taskId(taskId)
                        .contextId(contextId)
                        .artifact(artifact)
                        .append(append)
                        .lastChunk(lastChunk)
                        .build();

                sendStreamingEvent(artifactEvent);
            } else {
                finalResult = currentTask;
            }
        }

        private void sendDirectMessageInternal(Message message) {
            if (streaming) {
                sendStreamingEvent(message);
                markCompleted();
            } else {
                finalResult = message;
            }
        }

        private void sendStreamingEvent(Object event) {
            if (streamingConnection != null) {
                SendStreamingMessageResponse response = SendStreamingMessageResponse.builder()
                        .id(requestId)
                        .jsonrpc("2.0")
                        .result(event)
                        .build();

                streamingConnection.sendEvent(response);
            }
        }

        private void markCompleted() {
            if (!completed) {
                completed = true;
                if (streaming && streamingConnection != null) {
                    streamingConnection.close();
                    if (taskId != null) {
                        closeStreamingConnections(taskId);
                    }
                }
            }
        }

        private boolean isTerminalState(TaskState state) {
            return state == TaskState.COMPLETED ||
                    state == TaskState.CANCELED ||
                    state == TaskState.FAILED ||
                    state == TaskState.REJECTED;
        }

        private String generateArtifactId(String name) {
            // Generate artifact ID based on name for consistency
            return name.toLowerCase().replaceAll("[^a-z0-9]", "-") + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    /**
     * Internal class to manage streaming connections
     */
    private static class StreamingConnection {
        private final Object requestId;
        private final Consumer<Object> eventCallback;
        private final Runnable completeCallback;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        public StreamingConnection(Object requestId, Consumer<Object> eventCallback, Runnable completeCallback) {
            this.requestId = requestId;
            this.eventCallback = eventCallback;
            this.completeCallback = completeCallback;
        }

        public boolean sendEvent(Object event) {
            if (!closed.get()) {
                try {
                    eventCallback.accept(event);
                    return true;
                } catch (Exception e) {
                    // Connection might be broken
                    close();
                    return false;
                }
            }
            return false;
        }

        public synchronized void close() {
            if (closed.compareAndSet(false, true)) {
                try {
                    completeCallback.run();
                } catch (Exception e) {
                    // Ignore errors during close
                }
            }
        }
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }
}
