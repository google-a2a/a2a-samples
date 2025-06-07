package com.google.a2a.server.starter;

import com.google.a2a.model.*;
import com.google.a2a.server.core.A2AController;
import com.google.a2a.server.core.A2AServer;
import com.google.a2a.server.core.MessageHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A2A Server Auto Configuration for A2A specification v0.2.1
 */
@AutoConfiguration
@ConditionalOnClass({A2AServer.class, A2AController.class})
@EnableConfigurationProperties(A2AServerProperties.class)
public class A2AServerAutoConfiguration {

    @Autowired
    private A2AServerProperties properties;

    /**
     * Auto-configure A2AServer bean by scanning MessageHandler implementations
     */
    @Bean
    @ConditionalOnMissingBean
    public A2AServer a2aServer(List<MessageHandler> messageHandlers, ObjectMapper objectMapper) {
        if (messageHandlers.isEmpty()) {
            throw new IllegalStateException("No MessageHandler implementation found. Please implement MessageHandler interface.");
        }
        
        if (messageHandlers.size() > 1) {
            throw new IllegalStateException("Multiple MessageHandler implementations found. Please provide only one MessageHandler implementation.");
        }

        MessageHandler messageHandler = messageHandlers.get(0);
        AgentCard agentCard = createAgentCard();
        
        return new A2AServer(agentCard, messageHandler, objectMapper);
    }

    /**
     * Configure A2AController bean
     */
    @Bean
    @ConditionalOnMissingBean
    public A2AController a2aController(A2AServer a2aServer, ObjectMapper objectMapper) {
        return new A2AController(a2aServer, objectMapper);
    }

    /**
     * Configure ObjectMapper bean if not present
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Create AgentCard from configuration properties
     */
    private AgentCard createAgentCard() {
        // Create agent provider
        AgentProvider provider = AgentProvider.builder()
            .organization(properties.getProvider().getName())
            .url(properties.getProvider().getUrl())
            .build();

        // Create agent capabilities
        AgentCapabilities capabilities = AgentCapabilities.builder()
            .streaming(properties.isStreamingEnabled())
            .pushNotifications(properties.isPushNotificationsEnabled())
            .stateTransitionHistory(properties.isStateTransitionHistoryEnabled())
            .build();

        // Create security schemes if authentication is configured
        Map<String, SecurityScheme> securitySchemes = null;
        List<Map<String, List<String>>> security = null;
        
        if (properties.getAuthenticationMethods() != null && !properties.getAuthenticationMethods().isEmpty()) {
            // Convert authentication methods to security schemes
            securitySchemes = properties.getAuthenticationMethods().stream()
                .collect(Collectors.toMap(
                    method -> method.toLowerCase(),
                    method -> createSecurityScheme(method.toLowerCase())
                ));
            
            // Create security requirements
            security = List.of(
                properties.getAuthenticationMethods().stream()
                    .collect(Collectors.toMap(
                        method -> method.toLowerCase(),
                        method -> List.of()
                    ))
            );
        }

        // Create agent skills from configuration
        List<AgentSkill> skills = properties.getSkills().stream()
            .map(skillProps -> AgentSkill.builder()
                .id(skillProps.getId())
                .name(skillProps.getName())
                .description(skillProps.getDescription())
                .tags(skillProps.getTags())
                .examples(skillProps.getExamples())
                .inputModes(skillProps.getInputContentTypes())
                .outputModes(skillProps.getOutputContentTypes())
                .build())
            .collect(Collectors.toList());

        // If no skills configured, create a default one
        if (skills.isEmpty()) {
            skills = List.of(AgentSkill.builder()
                .id("default-skill")
                .name("Default Skill")
                .description("Default agent skill")
                .tags(List.of("default"))
                .examples(List.of("Hello", "What can you do?"))
                .inputModes(properties.getInputContentTypes())
                .outputModes(properties.getOutputContentTypes())
                .build());
        }

        return AgentCard.builder()
            .name(properties.getAgentName())
            .description(properties.getAgentDescription())
            .url(properties.getBaseUrl())
            .provider(provider)
            .version(properties.getAgentVersion())
            .documentationUrl(properties.getDocsUrl())
            .capabilities(capabilities)
            .securitySchemes(securitySchemes)
            .security(security)
            .defaultInputModes(properties.getInputContentTypes())
            .defaultOutputModes(properties.getOutputContentTypes())
            .skills(skills)
            .supportsAuthenticatedExtendedCard(properties.isSupportsAuthenticatedExtendedCard())
            .build();
    }

    /**
     * Create SecurityScheme based on authentication method
     */
    private SecurityScheme createSecurityScheme(String method) {
        switch (method.toLowerCase()) {
            case "bearer":
                return HTTPAuthSecurityScheme.builder()
                    .scheme("bearer")
                    .description("Bearer token authentication")
                    .build();
            case "basic":
                return HTTPAuthSecurityScheme.builder()
                    .scheme("basic")
                    .description("Basic authentication")
                    .build();
            case "apikey":
                return APIKeySecurityScheme.builder()
                    .name("X-API-Key")
                    .in("header")
                    .description("API key authentication")
                    .build();
            case "oauth2":
                // Create a simple OAuth2 scheme with authorization code flow
                return OAuth2SecurityScheme.builder()
                    .description("OAuth2 authentication")
                    .flows(OAuthFlows.builder()
                        .authorizationCode(AuthorizationCodeOAuthFlow.builder()
                            .authorizationUrl("https://example.com/oauth/authorize")
                            .tokenUrl("https://example.com/oauth/token")
                            .scopes(Map.of("read", "Read access"))
                            .build())
                        .build())
                    .build();
            default:
                // Default to Bearer token for unknown methods
                return HTTPAuthSecurityScheme.builder()
                    .scheme("bearer")
                    .description("Bearer token authentication")
                    .build();
        }
    }
} 