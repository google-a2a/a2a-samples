package com.google.a2a.client.example;

import com.google.a2a.client.core.A2AClient;
import com.google.a2a.client.core.A2AClientException;
import com.google.a2a.client.core.StreamingEventListener;
import com.google.a2a.model.AgentCard;
import com.google.a2a.model.Artifact;
import com.google.a2a.model.Message;
import com.google.a2a.model.MessageSendParams;
import com.google.a2a.model.Part;
import com.google.a2a.model.Task;
import com.google.a2a.model.TaskArtifactUpdateEvent;
import com.google.a2a.model.TaskIdParams;
import com.google.a2a.model.TaskQueryParams;
import com.google.a2a.model.TaskState;
import com.google.a2a.model.TaskStatusUpdateEvent;
import com.google.a2a.model.TextPart;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A2A Client Usage Example - AI Multilingual Translation Bot
 * Demonstrates the usage of A2A protocol v0.2.1 features
 */
public class A2AClientExample {

    public static void main(String[] args) throws Exception {
        // Create client with optional authentication headers
        Map<String, String> authHeaders = Map.of(
                // "Authorization", "Bearer your-token-here"
                // "X-API-Key", "your-api-key-here"
        );
        A2AClient client = new A2AClient("http://localhost:8080",
                java.net.http.HttpClient.newHttpClient(), authHeaders);

        // Get AI multilingual translation bot agent card
        System.out.println("=== Getting AI Multilingual Translation Bot Agent Card ===");
        try {
            AgentCard agentCard = client.getAgentCard();
            System.out.println("Agent Name: " + agentCard.name());
            System.out.println("Description: " + agentCard.description());
            System.out.println("Version: " + agentCard.version());
            System.out.println("Capabilities: " + agentCard.capabilities());

            // Try to get authenticated extended card if supported
            if (agentCard.supportsAuthenticatedExtendedCard() != null &&
                    agentCard.supportsAuthenticatedExtendedCard()) {
                try {
                    AgentCard extendedCard = client.getAuthenticatedExtendedCard();
                    System.out.println("Extended card retrieved with " +
                            extendedCard.skills().size() + " skills");
                } catch (A2AClientException e) {
                    System.out.println("Could not get extended card: " + e.getMessage());
                }
            }
        } catch (A2AClientException e) {
            System.err.println("Failed to get agent card: " + e.getMessage());
            return;
        }
        System.out.println();

        // Simple message exchange
        System.out.println("=== Simple Message Exchange ===");
        try {
            TextPart textPart = TextPart.builder()
                    .text("Hello, can you translate 'Good morning' from English to Spanish?")
                    .build();

            Message message = Message.builder()
                    .role("user")
                    .parts(List.of(textPart))
                    .messageId(UUID.randomUUID().toString())
                    .build();

            MessageSendParams params = MessageSendParams.builder()
                    .message(message)
                    .build();

            Object result = client.sendMessage(params);

            if (result instanceof Task task) {
                System.out.println("Task created with ID: " + task.id());
                System.out.println("Task status: " + task.status().state());

                if (task.artifacts() != null && !task.artifacts().isEmpty()) {
                    Artifact artifact = task.artifacts().get(0);
                    if (artifact.parts() != null && !artifact.parts().isEmpty()) {
                        Part part = artifact.parts().get(0);
                        if (part instanceof TextPart responsePart) {
                            System.out.println("Translation: " + responsePart.text());
                        }
                    }
                }
            } else if (result instanceof Message responseMessage) {
                System.out.println("Direct message response:");
                for (Part part : responseMessage.parts()) {
                    if (part instanceof TextPart responsePart) {
                        System.out.println("Response: " + responsePart.text());
                    }
                }
            }
        } catch (A2AClientException e) {
            System.err.println("Message send failed: " + e.getMessage());
            if (e.getErrorCode() != null) {
                System.err.println("Error code: " + e.getErrorCode());
            }
        }
        System.out.println();

        // Streaming translation example
        System.out.println("=== Streaming Translation Example ===");
        TextPart streamTextPart = TextPart.builder()
                .text("Please translate this detailed text about artificial intelligence: 'Artificial intelligence represents a revolutionary technological advancement that is transforming how we interact with machines and process information.'")
                .build();

        Message streamMessage = Message.builder()
                .role("user")
                .parts(List.of(streamTextPart))
                .messageId(UUID.randomUUID().toString())
                .build();

        MessageSendParams streamParams = MessageSendParams.builder()
                .message(streamMessage)
                .build();

        System.out.println("Request: " + streamTextPart.text());
        System.out.println("Streaming response:");

        AtomicReference<String> taskIdRef = new AtomicReference<>();
        CompletableFuture<Void> streamingFuture = client.sendMessageStreaming(streamParams, new StreamingEventListener() {
            @Override
            public void onEvent(Object event) {
                if (event instanceof Task task) {
                    taskIdRef.set(task.id());
                    System.out.println("Task update - Status: " + task.status().state());
                } else if (event instanceof TaskStatusUpdateEvent statusEvent) {
                    System.out.println("Status update: " + statusEvent.status().state());
                    if (statusEvent.status().message() != null) {
                        System.out.println("Status message: " + statusEvent.status().message());
                    }
                } else if (event instanceof TaskArtifactUpdateEvent artifactEvent) {
                    System.out.println("Artifact update for task: " + artifactEvent.taskId());
                    if (artifactEvent.artifact().parts() != null) {
                        for (Part part : artifactEvent.artifact().parts()) {
                            if (part instanceof TextPart textPart) {
                                System.out.print(textPart.text());
                            }
                        }
                    }
                } else if (event instanceof Message message) {
                    System.out.println("Message event from agent:");
                    for (Part part : message.parts()) {
                        if (part instanceof TextPart textPart) {
                            System.out.println("  " + textPart.text());
                        }
                    }
                } else {
                    System.out.println("Other event: " + event.getClass().getSimpleName());
                    System.out.println("  Content: " + event);
                }
            }

            @Override
            public void onError(Exception exception) {
                System.err.println("Streaming Error: " + exception.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("\nStreaming Completed");
            }
        });

        try {
            streamingFuture.get(60, TimeUnit.SECONDS);

            if (taskIdRef.get() != null) {
                TaskQueryParams queryParams = TaskQueryParams.builder()
                        .id(taskIdRef.get())
                        .historyLength(2)
                        .build();
                Task updatedTask = client.getTask(queryParams);
                if (updatedTask.artifacts() != null) {
                    for (Artifact artifact : updatedTask.artifacts()) {
                        if (artifact.parts() != null) {
                            for (Part part : artifact.parts()) {
                                if (part instanceof TextPart textPart) {
                                    System.out.println("Streaming translation result: " + textPart.text());
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("Streaming translation completed successfully");
        } catch (Exception e) {
            System.out.println("Streaming timeout or error: " + e.getMessage());
        }
        System.out.println();

        // Task management example
        System.out.println("=== Task Management Example ===");
        try {
            // Send a long-running task
            TextPart taskTextPart = TextPart.builder()
                    .text("Generate a comprehensive report on machine learning trends. This might take a while.")
                    .build();

            Message taskMessage = Message.builder()
                    .role("user")
                    .parts(List.of(taskTextPart))
                    .messageId(UUID.randomUUID().toString())
                    .build();

            MessageSendParams taskParams = MessageSendParams.builder()
                    .message(taskMessage)
                    .build();

            Object taskResult = client.sendMessage(taskParams);

            if (taskResult instanceof Task task) {
                String taskId = task.id();
                System.out.println("Long-running task created: " + taskId);

                boolean taskFinished = false;
                // Poll task status
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(1000); // Wait 1 second

                    TaskQueryParams queryParams = TaskQueryParams.builder()
                            .id(taskId)
                            .historyLength(2)
                            .build();

                    Task updatedTask = client.getTask(queryParams);
                    System.out.println("Task status: " + updatedTask.status().state());

                    if (TaskState.COMPLETED.equals(updatedTask.status().state()) ||
                            TaskState.FAILED.equals(updatedTask.status().state()) ||
                            TaskState.CANCELED.equals(updatedTask.status().state())) {
                        System.out.println("Task completed with state: " + updatedTask.status().state());
                        if (updatedTask.artifacts() != null) {
                            for (Artifact artifact : updatedTask.artifacts()) {
                                if (artifact.parts() != null) {
                                    for (Part part : artifact.parts()) {
                                        if (part instanceof TextPart textPart) {
                                            System.out.println("Task result: " + textPart.text());
                                        }
                                    }
                                }
                            }
                        }
                        taskFinished = true;
                        break;
                    }
                }

                if (!taskFinished) {
                    // Demonstrate task cancellation (if still running)
                    TaskIdParams cancelParams = TaskIdParams.builder()
                            .id(taskId)
                            .build();

                    try {
                        Task canceledTask = client.cancelTask(cancelParams);
                        System.out.println("Task cancellation result: " + canceledTask.status().state());
                    } catch (A2AClientException cancelException) {
                        System.out.println("Task cancellation failed: " + cancelException.getMessage());
                    }
                }
            }
        } catch (A2AClientException | InterruptedException e) {
            System.err.println("Task management example failed: " + e.getMessage());
        }

        System.out.println("=== A2A Client Example Completed ===");
    }
}
