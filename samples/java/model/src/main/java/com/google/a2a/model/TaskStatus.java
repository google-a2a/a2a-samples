package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.time.Instant;

/** TaskState and accompanying message. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskStatus(
    @JsonProperty("state")
    @NotNull(message = "Task state cannot be null")
    TaskState state,

    /** Additional status updates for client */
    @JsonProperty("message")
    @Valid
    Message message,

    /**
     * ISO 8601 datetime string when the status was recorded.
     * @example "2023-10-27T10:00:00Z"
     * */
    @JsonProperty("timestamp")
    String timestamp
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TaskState state;
        private Message message;
        private String timestamp;

        public Builder state(TaskState state) {
            this.state = state;
            return this;
        }

        public Builder message(Message message) {
            this.message = message;
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder timestampNow() {
            this.timestamp = Instant.now().toString();
            return this;
        }

        public TaskStatus build() {
            // Set current timestamp if not provided
            String finalTimestamp = timestamp != null ? timestamp : Instant.now().toString();

            TaskStatus status = new TaskStatus(state, message, finalTimestamp);
            return ValidationUtils.validateAndThrow(status);
        }
    }
}
