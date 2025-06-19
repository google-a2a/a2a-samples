package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageSendParams(
    /** The message being sent to the server. */
    @JsonProperty("message")
    @NotNull(message = "Message cannot be null")
    @Valid
    Message message,

    /** Send message configuration. */
    @JsonProperty("configuration")
    @Valid
    MessageSendConfiguration configuration,

    /** Extension metadata. */
    @JsonProperty("metadata")
    Map<String, Object> metadata
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Message message;
        private MessageSendConfiguration configuration;
        private Map<String, Object> metadata = Map.of(); // default empty

        public Builder message(Message message) {
            this.message = message;
            return this;
        }

        public Builder configuration(MessageSendConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }

        public MessageSendParams build() {
            MessageSendParams params = new MessageSendParams(message, configuration, metadata);
            return ValidationUtils.validateAndThrow(params);
        }
    }
}
