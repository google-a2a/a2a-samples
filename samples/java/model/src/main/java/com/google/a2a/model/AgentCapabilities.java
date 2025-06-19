package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Defines optional capabilities supported by an agent.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentCapabilities(
    /** true if the agent supports SSE. */
    @JsonProperty("streaming") Boolean streaming,
    /** true if the agent can notify updates to client. */
    @JsonProperty("pushNotifications") Boolean pushNotifications,
    /** true if the agent exposes status change history for tasks. */
    @JsonProperty("stateTransitionHistory") Boolean stateTransitionHistory,
    /** extensions supported by this agent. */
    @JsonProperty("extensions")
    @Valid
    List<AgentExtension> extensions
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean streaming = false; // default value
        private Boolean pushNotifications = false; // default value
        private Boolean stateTransitionHistory = false; // default value
        private List<AgentExtension> extensions = List.of(); // default empty list

        public Builder streaming(Boolean streaming) {
            this.streaming = streaming;
            return this;
        }

        public Builder pushNotifications(Boolean pushNotifications) {
            this.pushNotifications = pushNotifications;
            return this;
        }

        public Builder stateTransitionHistory(Boolean stateTransitionHistory) {
            this.stateTransitionHistory = stateTransitionHistory;
            return this;
        }

        public Builder extensions(List<AgentExtension> extensions) {
            this.extensions = extensions != null ? extensions : List.of();
            return this;
        }

        public AgentCapabilities build() {
            AgentCapabilities capabilities = new AgentCapabilities(streaming, pushNotifications, stateTransitionHistory, extensions);
            return ValidationUtils.validateAndThrow(capabilities);
        }
    }
}
