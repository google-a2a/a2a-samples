package com.google.a2a.server.example;

import com.google.a2a.server.core.MessageHandler;
import com.google.a2a.server.core.TaskContext;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Translation Task Handler using Google Agent Development Kit (ADK)
 * Implements A2A specification v0.2.1 MessageHandler interface
 * Uses Google ADK LlmAgent for translation
 */
@Component
public class TranslationTaskHandler implements MessageHandler {

    private final InMemoryRunner runner;

    public TranslationTaskHandler(
            @Value("${google.cloud.ai.model:gemini-2.0-flash}") String modelName) {

        LlmAgent llmAgent = LlmAgent.builder()
                .name("translation-agent")
                .description("Professional multilingual translator")
                .model(modelName)
                .instruction("You are a professional multilingual translator. Follow the translation rules provided in each request.")
                .build();

        this.runner = new InMemoryRunner(llmAgent);
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
            context.setWorking("Starting translation with Google ADK...");

            // Create translation prompt
            String translationPrompt = createTranslationPrompt(textToTranslate);

            if (context.isStreaming()) {
                // Use streaming with Google ADK
                performStreamingTranslation(context, translationPrompt);
            } else {
                // For synchronous, use standard call
                performSynchronousTranslation(context, translationPrompt);
            }

        } catch (Exception e) {
            // Handle errors
            context.setFailed("Translation failed: " + e.getMessage());
        }
    }

    /**
     * Perform synchronous translation using Google ADK
     */
    private void performSynchronousTranslation(TaskContext context, String translationPrompt) {
        try {
            // Create a session for the translation request
            Session session = runner.sessionService()
                    .createSession(runner.appName(), "user-" + System.currentTimeMillis())
                    .blockingGet();

            Content userMessage = Content.fromParts(Part.fromText(translationPrompt));

            // Run the agent and collect results
            final StringBuilder fullResponse = new StringBuilder();

            Flowable<Event> events = runner.runAsync(session.userId(), session.id(), userMessage);

            events.blockingForEach(event -> {
                if (event.finalResponse() && event.content().isPresent()) {
                    event.content().get().parts().stream()
                            .flatMap(List::stream)
                            .map(Part::text)
                            .forEach(text -> text.ifPresent(fullResponse::append));
                }
            });

            String translatedText = fullResponse.toString().trim();
            if (!translatedText.isEmpty()) {
                context.addTextArtifact("Translation Result", translatedText);
                context.setCompleted("Translation completed successfully with Google ADK.");
            } else {
                context.setFailed("Translation failed: Empty response from Google ADK.");
            }

        } catch (Exception e) {
            context.setFailed("Translation failed: " + e.getMessage());
        }
    }

    /**
     * Perform streaming translation using Google ADK
     */
    private void performStreamingTranslation(TaskContext context, String translationPrompt) {
        try {
            // Create a session for the translation request
            Session session = runner.sessionService()
                    .createSession(runner.appName(), "user-" + System.currentTimeMillis())
                    .blockingGet();

            Content userMessage = Content.fromParts(Part.fromText(translationPrompt));

            Flowable<Event> events = runner.runAsync(session.userId(), session.id(), userMessage);

            events.subscribe(
                    event -> {
                        // Handle each event
                        if (event.content().isPresent()) {
                            event.content().get().parts().stream()
                                    .flatMap(List::stream)
                                    .map(Part::text)
                                    .forEach(chunk -> {
                                        chunk.ifPresent(text -> {
                                            if (!text.trim().isEmpty()) {
                                                // Send streaming artifact update with accumulated content
                                                context.addTextArtifact("Translation Result", text,
                                                        "Streaming translation result from Google ADK", false, false);
                                            }
                                        });
                                    });
                        }
                    },
                    error -> {
                        // Handle error
                        context.setFailed("Streaming translation failed: " + error.getMessage());
                    },
                    () -> {
                        // Handle completion
                        context.setCompleted("Translation completed successfully with Google ADK.");
                    }
            );

        } catch (Exception e) {
            context.setFailed("Streaming translation failed: " + e.getMessage());
        }
    }

    /**
     * Create translation prompt for Google ADK
     */
    private String createTranslationPrompt(String text) {
        return String.format("""
                Please translate the following text according to these rules:
                
                Translation Rules:
                1. If the text is in English, translate to Spanish
                2. If the text is in Spanish, translate to French
                3. If the text is in French, translate to German
                4. If the text is in German, translate to Italian
                5. If the text is in Italian, translate to English
                6. If the text is in any other language, translate to English
                
                Quality Guidelines:
                - Maintain the original meaning, tone, and context with cultural sensitivity
                - Provide natural, fluent translations appropriate for native speakers
                - Use formal register unless the original text is clearly informal
                - Only return the translated text, no explanations or additional content
                
                Text to translate: %s
                """, text);
    }
}

