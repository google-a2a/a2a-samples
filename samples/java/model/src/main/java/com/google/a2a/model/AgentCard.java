package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * An AgentCard conveys key information:
 * - Overall details (version, name, description, uses)
 * - Skills: A set of capabilities the agent can perform
 * - Default modalities/content types supported by the agent.
 * - Authentication requirements
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentCard(
    /**
     * Human readable name of the agent.
     * @example "Recipe Agent"
     */
    @JsonProperty("name")
    @NotBlank(message = "Agent name cannot be blank")
    String name,
    /**
     * A human-readable description of the agent. Used to assist users and
     * other agents in understanding what the agent can do.
     * @example "Agent that helps users with recipes and cooking."
     */
    @JsonProperty("description")
    @NotBlank(message = "Agent description cannot be blank")
    String description,
    /** A URL to the address the agent is hosted at. */
    @JsonProperty("url")
    @NotBlank(message = "Agent URL cannot be blank")
    @Pattern(regexp = "^https?://.*", message = "Agent URL must be a valid HTTP/HTTPS URL")
    String url,
    /** A URL to an icon for the agent. */
    @JsonProperty("iconUrl")
    String iconUrl,
    /** The service provider of the agent */
    @JsonProperty("provider")
    @Valid
    AgentProvider provider,
    /**
     * The version of the agent - format is up to the provider.
     * @example "1.0.0"
     */
    @JsonProperty("version")
    @NotBlank(message = "Agent version cannot be blank")
    String version,
    /** A URL to documentation for the agent. */
    @JsonProperty("documentationUrl")
    String documentationUrl,
    /** Optional capabilities supported by the agent. */
    @JsonProperty("capabilities")
    @NotNull(message = "Agent capabilities cannot be null")
    @Valid
    AgentCapabilities capabilities,
    /** Security scheme details used for authenticating with this agent. */
    @JsonProperty("securitySchemes")
    Map<String, SecurityScheme> securitySchemes,
    /** Security requirements for contacting the agent. */
    @JsonProperty("security")
    List<Map<String, List<String>>> security,
    /**
     * The set of interaction modes that the agent supports across all skills. This can be overridden per-skill.
     * Supported media types for input.
     */
    @JsonProperty("defaultInputModes")
    @NotNull(message = "Default input modes cannot be null")
    @NotEmpty(message = "Agent must support at least one input mode")
    List<String> defaultInputModes,
    /** Supported media types for output. */
    @JsonProperty("defaultOutputModes")
    @NotNull(message = "Default output modes cannot be null")
    @NotEmpty(message = "Agent must support at least one output mode")
    List<String> defaultOutputModes,
    /** Skills are a unit of capability that an agent can perform. */
    @JsonProperty("skills")
    @NotNull(message = "Agent skills cannot be null")
    @NotEmpty(message = "Agent must have at least one skill")
    @Valid
    List<AgentSkill> skills,
    /**
     * true if the agent supports providing an extended agent card when the user is authenticated.
     * Defaults to false if not specified.
     */
    @JsonProperty("supportsAuthenticatedExtendedCard")
    Boolean supportsAuthenticatedExtendedCard
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private String url;
        private String iconUrl;
        private AgentProvider provider;
        private String version;
        private String documentationUrl;
        private AgentCapabilities capabilities = AgentCapabilities.builder().build(); // default
        private Map<String, SecurityScheme> securitySchemes = Map.of(); // default empty
        private List<Map<String, List<String>>> security = List.of(); // default empty
        private List<String> defaultInputModes = List.of("text/plain"); // default
        private List<String> defaultOutputModes = List.of("text/plain"); // default
        private List<AgentSkill> skills = List.of(); // default empty
        private Boolean supportsAuthenticatedExtendedCard = false; // default

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Builder provider(AgentProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder documentationUrl(String documentationUrl) {
            this.documentationUrl = documentationUrl;
            return this;
        }

        public Builder capabilities(AgentCapabilities capabilities) {
            this.capabilities = capabilities != null ? capabilities : AgentCapabilities.builder().build();
            return this;
        }

        public Builder securitySchemes(Map<String, SecurityScheme> securitySchemes) {
            this.securitySchemes = securitySchemes != null ? securitySchemes : Map.of();
            return this;
        }

        public Builder security(List<Map<String, List<String>>> security) {
            this.security = security != null ? security : List.of();
            return this;
        }

        public Builder defaultInputModes(List<String> defaultInputModes) {
            this.defaultInputModes = defaultInputModes != null ? defaultInputModes : List.of("text/plain");
            return this;
        }

        public Builder defaultOutputModes(List<String> defaultOutputModes) {
            this.defaultOutputModes = defaultOutputModes != null ? defaultOutputModes : List.of("text/plain");
            return this;
        }

        public Builder skills(List<AgentSkill> skills) {
            this.skills = skills != null ? skills : List.of();
            return this;
        }

        public Builder supportsAuthenticatedExtendedCard(Boolean supportsAuthenticatedExtendedCard) {
            this.supportsAuthenticatedExtendedCard = supportsAuthenticatedExtendedCard != null ? supportsAuthenticatedExtendedCard : false;
            return this;
        }

        public AgentCard build() {
            AgentCard card = new AgentCard(name, description, url, iconUrl, provider, version, documentationUrl,
                    capabilities, securitySchemes, security, defaultInputModes, defaultOutputModes,
                    skills, supportsAuthenticatedExtendedCard);
            return ValidationUtils.validateAndThrow(card);
        }
    }
}
