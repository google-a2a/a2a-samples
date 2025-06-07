package com.google.a2a.server.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.a2a.model.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * A2A REST controller for handling JSON-RPC requests based on A2A specification v0.2.1
 */
@RestController
public class A2AController {

    private final A2AServer server;
    private final ObjectMapper objectMapper;

    public A2AController(A2AServer server, ObjectMapper objectMapper) {
        this.server = server;
        this.objectMapper = objectMapper;
    }

    /**
     * Handle JSON-RPC requests (non-streaming)
     */
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<JSONRPCResponse> handleJsonRpcRequest(@RequestBody JSONRPCRequest request) {

        if (!"2.0".equals(request.jsonrpc())) {
            return ResponseEntity.badRequest().body(createErrorResponse(request.id(), -32600, "Invalid JSON-RPC version"));
        }

        JSONRPCResponse response = switch (request.method()) {
            case "message/send" -> server.handleMessageSend(request);
            case "tasks/get" -> server.handleTaskGet(request);
            case "tasks/cancel" -> server.handleTaskCancel(request);
            case "tasks/pushNotificationConfig/set" -> server.handleSetPushNotificationConfig(request);
            case "tasks/pushNotificationConfig/get" -> server.handleGetPushNotificationConfig(request);
            default -> createErrorResponse(request.id(), -32601, "Method not found: " + request.method());
        };

        return ResponseEntity.ok(response);
    }

    /**
     * Handle streaming JSON-RPC requests (Server-Sent Events)
     * Supports message/stream and tasks/resubscribe methods
     */
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter handleStreamingRequest(@RequestBody JSONRPCRequest request) {

        if (!"2.0".equals(request.jsonrpc())) {
            return createErrorSseEmitter(request.id(), -32600, "Invalid JSON-RPC version");
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Process request asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                switch (request.method()) {
                    case "message/stream" -> {
                        server.handleMessageStream(request, event -> sendSseEvent(emitter, event), emitter::complete);
                    }
                    case "tasks/resubscribe" -> {
                        server.handleTaskResubscribe(request, event -> sendSseEvent(emitter, event), emitter::complete);
                    }
                    default -> {
                        sendErrorSseEvent(emitter, request.id(), -32601, "Method not found: " + request.method());
                    }
                }
            } catch (Exception e) {
                sendErrorSseEvent(emitter, request.id(), -32603, "Internal error: " + e.getMessage());
            }
        });

        return emitter;
    }

    /**
     * Get public agent card information
     */
    @GetMapping("/.well-known/agent.json")
    public ResponseEntity<AgentCard> getAgentCard() {
        return ResponseEntity.ok(server.getAgentCard());
    }

    /**
     * Get authenticated extended agent card (requires authentication)
     */
    @GetMapping("/agent/authenticatedExtendedCard")
    public ResponseEntity<AgentCard> getAuthenticatedExtendedCard() {
        // In a real implementation, you would verify authentication here
        // For this example, we just return the extended card
        AgentCard extendedCard = server.getAuthenticatedExtendedCard();
        return ResponseEntity.ok(extendedCard);
    }

    /**
     * Handle preflight OPTIONS requests for CORS
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok()
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            .header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-API-Key")
            .build();
    }

    /**
     * Send SSE event
     */
    private void sendSseEvent(SseEmitter emitter, Object event) {
        try {
            String eventData = objectMapper.writeValueAsString(event);
            emitter.send(SseEmitter.event()
                .name("data")
                .data(eventData));
        } catch (IOException e) {
            emitter.completeWithError(e);
        } catch (Exception e) {
            sendErrorSseEvent(emitter, null, -32603, "Failed to serialize event: " + e.getMessage());
        }
    }

    /**
     * Send error SSE event
     */
    private void sendErrorSseEvent(SseEmitter emitter, Object requestId, int code, String message) {
        try {
            JSONRPCError error = JSONRPCError.builder()
                .code(code)
                .message(message)
                .build();

            SendStreamingMessageResponse errorResponse = SendStreamingMessageResponse.builder()
                .id(requestId)
                .jsonrpc("2.0")
                .error(error)
                .build();

            String eventData = objectMapper.writeValueAsString(errorResponse);
            emitter.send(SseEmitter.event()
                .name("data")
                .data(eventData));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    /**
     * Create error SSE emitter
     */
    private SseEmitter createErrorSseEmitter(Object requestId, int code, String message) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        sendErrorSseEvent(emitter, requestId, code, message);
        return emitter;
    }

    /**
     * Create JSON-RPC error response
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
}
