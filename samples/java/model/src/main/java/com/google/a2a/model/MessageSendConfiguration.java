package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;

/**Configuration for the send message request. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageSendConfiguration(
    /** Accepted output modalities by the client. */
    @JsonProperty("acceptedOutputModes")
    @NotNull(message = "Accepted output modes cannot be null")
    @NotEmpty(message = "Must specify at least one accepted output mode")
    List<String> acceptedOutputModes,

    /** Number of recent messages to be retrieved. */
    @JsonProperty("historyLength")
    @Min(value = 0, message = "History length must be non-negative")
    Integer historyLength,

    /** Where the server should send notifications when disconnected. */
    @JsonProperty("pushNotificationConfig")
    @Valid
    PushNotificationConfig pushNotificationConfig,

    /** If the server should treat the client as a blocking request. */
    @JsonProperty("blocking")
    Boolean blocking
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> acceptedOutputModes = List.of("text/plain"); // default
        private Integer historyLength = 10; // default
        private PushNotificationConfig pushNotificationConfig;
        private Boolean blocking;

        public Builder acceptedOutputModes(List<String> acceptedOutputModes) {
            this.acceptedOutputModes = acceptedOutputModes != null ? acceptedOutputModes : List.of("text/plain");
            return this;
        }

        public Builder historyLength(Integer historyLength) {
            this.historyLength = historyLength;
            return this;
        }

        public Builder pushNotificationConfig(PushNotificationConfig pushNotificationConfig) {
            this.pushNotificationConfig = pushNotificationConfig;
            return this;
        }

        public Builder blocking(Boolean blocking) {
            this.blocking = blocking;
            return this;
        }

        public MessageSendConfiguration build() {
            MessageSendConfiguration config = new MessageSendConfiguration(acceptedOutputModes, historyLength, pushNotificationConfig, blocking);
            return ValidationUtils.validateAndThrow(config);
        }
    }
}
