package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Task(
    /** Unique identifier for the task */
    @JsonProperty("id")
    @NotBlank(message = "Task ID cannot be blank")
    String id,

    /** Server-generated id for contextual alignment across interactions */
    @JsonProperty("contextId")
    @NotBlank(message = "Context ID cannot be blank")
    String contextId,

    /** Current status of the task */
    @JsonProperty("status")
    @NotNull(message = "Task status cannot be null")
    @Valid
    TaskStatus status,

    @JsonProperty("history")
    @Valid
    List<Message> history,

    /** Collection of artifacts created by the agent. */
    @JsonProperty("artifacts")
    @Valid
    List<Artifact> artifacts,

    /** Extension metadata. */
    @JsonProperty("metadata")
    Map<String, Object> metadata,

    /** Event type */
    @JsonProperty("kind")
    @Pattern(regexp = "^task$", message = "Kind must be 'task'")
    String kind
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String contextId;
        private TaskStatus status;
        private List<Message> history = List.of(); // default empty
        private List<Artifact> artifacts = List.of(); // default empty
        private Map<String, Object> metadata = Map.of(); // default empty
        private String kind = "task"; // default

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder history(List<Message> history) {
            this.history = history != null ? history : List.of();
            return this;
        }

        public Builder artifacts(List<Artifact> artifacts) {
            this.artifacts = artifacts != null ? artifacts : List.of();
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }

        public Builder kind(String kind) {
            this.kind = kind != null ? kind : "task";
            return this;
        }

        public Task build() {
            Task task = new Task(id, contextId, status, history, artifacts, metadata, kind);
            return ValidationUtils.validateAndThrow(task);
        }
    }
}
