package com.google.a2a.server.core;

import com.google.a2a.model.*;

/**
 * Context for task processing that provides simplified methods for business logic
 * All task management is handled automatically by A2AServer
 */
public interface TaskContext {
    
    /**
     * Set working status with optional message
     * @param message Optional status message
     */
    void setWorking(String message);
    
    /**
     * Set input required status with message
     * @param message Message asking for input
     */
    void setInputRequired(String message);
    
    /**
     * Set auth required status with message
     * @param message Message describing auth requirements
     */
    void setAuthRequired(String message);
    
    /**
     * Set completed status with optional message
     * @param message Optional completion message
     */
    void setCompleted(String message);
    
    /**
     * Set failed status with error message
     * @param errorMessage Error description
     */
    void setFailed(String errorMessage);
    
    /**
     * Add text artifact to the task
     * @param name Artifact name
     * @param text Text content
     * @param description Optional description
     * @param append Whether to append to existing artifact with same name
     * @param lastChunk Whether this is the final chunk
     */
    void addTextArtifact(String name, String text, String description, boolean append, boolean lastChunk);
    
    /**
     * Add text artifact to the task (convenience method)
     * @param name Artifact name
     * @param text Text content
     */
    void addTextArtifact(String name, String text);
    
    /**
     * Add file artifact to the task
     * @param name Artifact name
     * @param fileName File name
     * @param mimeType MIME type
     * @param fileContent Base64 encoded file content or URI
     * @param isUri Whether fileContent is URI (true) or base64 bytes (false)
     * @param description Optional description
     */
    void addFileArtifact(String name, String fileName, String mimeType, String fileContent, boolean isUri, String description);
    
    /**
     * Add structured data artifact to the task
     * @param name Artifact name
     * @param data Structured data as Map
     * @param description Optional description
     */
    void addDataArtifact(String name, Object data, String description);
    
    /**
     * Send a direct message response (bypasses task system)
     * @param text Response text
     */
    void sendDirectMessage(String text);
    
    /**
     * Send a direct message response with structured data
     * @param text Response text
     * @param data Additional structured data
     */
    void sendDirectMessage(String text, Object data);
    
    /**
     * Get the current user message for processing
     * @return The user message
     */
    Message getUserMessage();
    
    /**
     * Extract text content from user message
     * @return Combined text from all text parts
     */
    String extractUserText();
    
    /**
     * Extract file parts from user message
     * @return List of file parts
     */
    java.util.List<FilePart> extractUserFiles();
    
    /**
     * Extract data parts from user message
     * @return List of data parts
     */
    java.util.List<DataPart> extractUserData();
    
    /**
     * Check if this context is for streaming
     * @return true if streaming, false if synchronous
     */
    boolean isStreaming();
    
    /**
     * Get the task ID (available after first status update)
     * @return The task ID or null if no task created yet
     */
    String getTaskId();
    
    /**
     * Get the context ID
     * @return The context ID
     */
    String getContextId();
    
    /**
     * Get request metadata
     * @return Request metadata
     */
    java.util.Map<String, Object> getMetadata();
} 