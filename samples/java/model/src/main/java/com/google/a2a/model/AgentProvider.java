package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Represents the service provider of an agent.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentProvider(
    /** Agent provider's organization name. */
    @JsonProperty("organization")
    @NotBlank(message = "Organization name cannot be blank")
    String organization,

    /** Agent provider's URL. */
    @JsonProperty("url")
    @NotBlank(message = "URL cannot be blank")
    @Pattern(regexp = "^https?://.*", message = "URL must be a valid HTTP/HTTPS URL")
    String url
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String organization;
        private String url;

        public Builder organization(String organization) {
            this.organization = organization;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public AgentProvider build() {
            AgentProvider provider = new AgentProvider(organization, url);
            return ValidationUtils.validateAndThrow(provider);
        }
    }
}
