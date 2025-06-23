package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * A declaration of an extension supported by an Agent.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentExtension(
    /** The URI of the extension. */
    @JsonProperty("uri")
    @NotBlank(message = "Extension URI cannot be blank")
    String uri,
    /** A description of how this agent uses this extension. */
    @JsonProperty("description") String description,
    /** Whether the client must follow specific requirements of the extension. */
    @JsonProperty("required") Boolean required,
    /** Optional configuration for the extension. */
    @JsonProperty("params") Map<String, Object> params
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uri;
        private String description;
        private Boolean required = false; // default value
        private Map<String, Object> params = Map.of(); // default empty map

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder required(Boolean required) {
            this.required = required != null ? required : false;
            return this;
        }

        public Builder params(Map<String, Object> params) {
            this.params = params != null ? params : Map.of();
            return this;
        }

        public AgentExtension build() {
            AgentExtension extension = new AgentExtension(uri, description, required, params);
            return ValidationUtils.validateAndThrow(extension);
        }
    }
}
