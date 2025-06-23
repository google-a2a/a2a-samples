package com.google.a2a.server.core;

import com.google.a2a.model.MessageSendParams;

/**
 * Interface for handling A2A message processing
 * Implementations should focus only on business logic
 * All task management is handled automatically by A2AServer
 */
public interface MessageHandler {
    
    /**
     * Handle a message and perform business logic
     * Use TaskContext methods to update status and add artifacts
     * A2AServer will automatically create and manage tasks
     * 
     * @param context Task context providing simplified business logic methods
     */
    void handleMessage(TaskContext context);
}
