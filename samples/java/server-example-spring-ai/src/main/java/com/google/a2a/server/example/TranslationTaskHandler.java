package com.google.a2a.server.example;

import com.google.a2a.model.*;
import com.google.a2a.server.core.MessageHandler;
import com.google.a2a.server.core.TaskContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Translation Task Handler for AI translation service
 * Implements A2A specification v0.2.1 MessageHandler interface
 * Focuses purely on business logic while A2AServer handles task management
 */
@Component
public class TranslationTaskHandler implements MessageHandler {

    private final ChatClient chatClient;

    public TranslationTaskHandler(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }

    @Override
    public void handleMessage(TaskContext context) {
        try {
            // Extract text content from user message
            String textToTranslate = context.extractUserText();

            if (textToTranslate == null || textToTranslate.trim().isEmpty()) {
                // Send direct response for empty requests
                context.sendDirectMessage("Please provide text to translate.");
                return;
            }

            // Set working status
            context.setWorking("Starting translation...");

            // Create translation prompt
            String translationPrompt = createTranslationPrompt(textToTranslate);

            if (context.isStreaming()) {
                // Use real streaming with ChatClient
                performStreamingTranslation(context, translationPrompt);
            } else {
                // For synchronous, use standard call
                String translatedText = chatClient
                    .prompt(translationPrompt)
                    .call()
                    .content();

                context.addTextArtifact("Translation Result", translatedText);
                context.setCompleted("Translation completed successfully.");
            }

        } catch (Exception e) {
            // Handle errors
            context.setFailed("Translation failed: " + e.getMessage());
        }
    }

    /**
     * Perform real streaming translation using ChatClient stream mode
     */
    private void performStreamingTranslation(TaskContext context, String translationPrompt) {
        try {
            // Use ChatClient streaming
            Flux<ChatResponse> responseFlux = chatClient
                .prompt(translationPrompt)
                .stream()
                .chatResponse();


            responseFlux
                .doOnNext(chatResponse -> {
                    // Process each streaming chunk
                    if (chatResponse != null && chatResponse.getResults() != null) {
                        for (Generation generation : chatResponse.getResults()) {
                            String chunk = generation.getOutput().getText();
                            if (chunk != null && !chunk.isEmpty()) {

                                // Send streaming artifact update with accumulated content
                                context.addTextArtifact("Translation Result", chunk,
                                    "Streaming translation result", false, false);
                            }
                        }
                    }
                })
                .doOnComplete(() -> {
                    // Mark the final chunk when streaming is complete
                    context.setCompleted("Translation completed successfully.");
                })
                .doOnError(error -> {
                    context.setFailed("Streaming translation failed: " + error.getMessage());
                })
                .blockLast(); // Block to wait for completion since this is called synchronously

        } catch (Exception e) {
            context.setFailed("Streaming translation failed: " + e.getMessage());
        }
    }

    /**
     * Create translation prompt for ChatClient
     */
    private String createTranslationPrompt(String text) {
        return String.format("""
            You are a professional multilingual translator. Please translate the following text according to these rules:
            
            Instructions:
            1. If the text is in English, translate to Spanish
            2. If the text is in Spanish, translate to French
            3. If the text is in French, translate to German
            4. If the text is in German, translate to Italian
            5. If the text is in Italian, translate to English
            6. If the text is in any other language, translate to English
            7. Maintain the original meaning, tone, and context
            8. Provide natural, fluent translations appropriate for native speakers
            9. Only return the translated text, no explanations or additional content
            
            Text to translate: %s
            """, text);
    }
}
