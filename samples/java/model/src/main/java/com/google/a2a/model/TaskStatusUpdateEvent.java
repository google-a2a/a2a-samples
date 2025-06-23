package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import java.util.Map;

/** Sent by server during sendStream or subscribe requests */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskStatusUpdateEvent(
    /** Task id */
    @JsonProperty("taskId")
    @NotBlank(message = "Task ID cannot be blank")
    String taskId,

    /** The context the task is associated with */
    @JsonProperty("contextId")
    @NotBlank(message = "Context ID cannot be blank")
    String contextId,

    /** Event type */
    @JsonProperty("kind")
    @Pattern(regexp = "^status-update$", message = "Kind must be 'status-update'")
    String kind,

    /** Current status of the task */
    @JsonProperty("status")
    @NotNull(message = "Task status cannot be null")
    @Valid
    TaskStatus status,

    /** Indicates the end of the event stream */
    @JsonProperty("final")
    @NotNull(message = "Final flag cannot be null")
    Boolean finalEvent,

    /** Extension metadata. */
    @JsonProperty("metadata")
    Map<String, Object> metadata
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String taskId;
        private String contextId;
        private String kind = "status-update"; // default
        private TaskStatus status;
        private Boolean finalEvent = false; // default
        private Map<String, Object> metadata = Map.of(); // default empty

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder kind(String kind) {
            this.kind = kind != null ? kind : "status-update";
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder finalEvent(Boolean finalEvent) {
            this.finalEvent = finalEvent != null ? finalEvent : false;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }

        public TaskStatusUpdateEvent build() {
            TaskStatusUpdateEvent event = new TaskStatusUpdateEvent(taskId, contextId, kind, status, finalEvent, metadata);
            return ValidationUtils.validateAndThrow(event);
        }
    }
}
