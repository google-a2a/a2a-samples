package com.google.a2a.server.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * A2A Server Configuration Properties for A2A specification v0.2.1
 */
@ConfigurationProperties(prefix = "a2a.server")
public class A2AServerProperties {

    /**
     * Agent name
     */
    private String agentName = "A2A Agent";

    /**
     * Agent description
     */
    private String agentDescription = "A2A Agent powered by Spring Boot";

    /**
     * Agent version
     */
    private String agentVersion = "1.0.0";

    /**
     * Server base URL
     */
    private String baseUrl = "http://localhost:8080";

    /**
     * Documentation URL
     */
    private String docsUrl = "http://localhost:8080/docs";

    /**
     * Icon URL
     */
    private String iconUrl;

    /**
     * Enable streaming support (message/stream, tasks/resubscribe)
     */
    private boolean streamingEnabled = true;

    /**
     * Enable push notifications (tasks/pushNotificationConfig/*)
     */
    private boolean pushNotificationsEnabled = true;

    /**
     * Enable state transition history
     */
    private boolean stateTransitionHistoryEnabled = true;

    /**
     * Support authenticated extended card
     */
    private boolean supportsAuthenticatedExtendedCard = true;

    /**
     * Agent provider configuration
     */
    private AgentProviderProperties provider = new AgentProviderProperties();

    /**
     * Agent skills configuration
     */
    private List<AgentSkillProperties> skills = List.of();

    /**
     * Default input content types (Media Types)
     */
    private List<String> inputContentTypes = List.of("application/json", "text/plain");

    /**
     * Default output content types (Media Types)
     */
    private List<String> outputContentTypes = List.of("application/json", "text/plain");

    /**
     * Authentication methods (for backward compatibility, converted to security schemes)
     */
    private List<String> authenticationMethods = List.of();

    // Getters and setters
    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentDescription() {
        return agentDescription;
    }

    public void setAgentDescription(String agentDescription) {
        this.agentDescription = agentDescription;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDocsUrl() {
        return docsUrl;
    }

    public void setDocsUrl(String docsUrl) {
        this.docsUrl = docsUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public boolean isStreamingEnabled() {
        return streamingEnabled;
    }

    public void setStreamingEnabled(boolean streamingEnabled) {
        this.streamingEnabled = streamingEnabled;
    }

    public boolean isPushNotificationsEnabled() {
        return pushNotificationsEnabled;
    }

    public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) {
        this.pushNotificationsEnabled = pushNotificationsEnabled;
    }

    public boolean isStateTransitionHistoryEnabled() {
        return stateTransitionHistoryEnabled;
    }

    public void setStateTransitionHistoryEnabled(boolean stateTransitionHistoryEnabled) {
        this.stateTransitionHistoryEnabled = stateTransitionHistoryEnabled;
    }

    public boolean isSupportsAuthenticatedExtendedCard() {
        return supportsAuthenticatedExtendedCard;
    }

    public void setSupportsAuthenticatedExtendedCard(boolean supportsAuthenticatedExtendedCard) {
        this.supportsAuthenticatedExtendedCard = supportsAuthenticatedExtendedCard;
    }

    public AgentProviderProperties getProvider() {
        return provider;
    }

    public void setProvider(AgentProviderProperties provider) {
        this.provider = provider;
    }

    public List<AgentSkillProperties> getSkills() {
        return skills;
    }

    public void setSkills(List<AgentSkillProperties> skills) {
        this.skills = skills;
    }

    public List<String> getInputContentTypes() {
        return inputContentTypes;
    }

    public void setInputContentTypes(List<String> inputContentTypes) {
        this.inputContentTypes = inputContentTypes;
    }

    public List<String> getOutputContentTypes() {
        return outputContentTypes;
    }

    public void setOutputContentTypes(List<String> outputContentTypes) {
        this.outputContentTypes = outputContentTypes;
    }

    public List<String> getAuthenticationMethods() {
        return authenticationMethods;
    }

    public void setAuthenticationMethods(List<String> authenticationMethods) {
        this.authenticationMethods = authenticationMethods;
    }

    /**
     * Agent provider configuration
     */
    public static class AgentProviderProperties {
        private String name = "A2A Provider";
        private String url = "https://example.com";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    /**
     * Agent skill configuration
     */
    public static class AgentSkillProperties {
        private String id;
        private String name;
        private String description;
        private List<String> tags = List.of();
        private List<String> examples = List.of();
        private List<String> inputContentTypes = List.of("application/json", "text/plain");
        private List<String> outputContentTypes = List.of("application/json", "text/plain");

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public List<String> getExamples() {
            return examples;
        }

        public void setExamples(List<String> examples) {
            this.examples = examples;
        }

        public List<String> getInputContentTypes() {
            return inputContentTypes;
        }

        public void setInputContentTypes(List<String> inputContentTypes) {
            this.inputContentTypes = inputContentTypes;
        }

        public List<String> getOutputContentTypes() {
            return outputContentTypes;
        }

        public void setOutputContentTypes(List<String> outputContentTypes) {
            this.outputContentTypes = outputContentTypes;
        }
    }
} 