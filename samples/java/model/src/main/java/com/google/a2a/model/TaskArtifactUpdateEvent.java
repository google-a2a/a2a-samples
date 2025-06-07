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
public record TaskArtifactUpdateEvent(
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
    @Pattern(regexp = "^artifact-update$", message = "Kind must be 'artifact-update'")
    String kind,

    /** Generated artifact */
    @JsonProperty("artifact")
    @NotNull(message = "Artifact cannot be null")
    @Valid
    Artifact artifact,

    /** Indicates if this artifact appends to a previous one */
    @JsonProperty("append")
    Boolean append,

    /** Indicates if this is the last chunk of the artifact */
    @JsonProperty("lastChunk")
    Boolean lastChunk,

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
        private String kind = "artifact-update"; // default
        private Artifact artifact;
        private Boolean append = false; // default
        private Boolean lastChunk = false; // default
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
            this.kind = kind != null ? kind : "artifact-update";
            return this;
        }

        public Builder artifact(Artifact artifact) {
            this.artifact = artifact;
            return this;
        }

        public Builder append(Boolean append) {
            this.append = append != null ? append : false;
            return this;
        }

        public Builder lastChunk(Boolean lastChunk) {
            this.lastChunk = lastChunk != null ? lastChunk : false;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }

        public TaskArtifactUpdateEvent build() {
            TaskArtifactUpdateEvent event = new TaskArtifactUpdateEvent(taskId, contextId, kind, artifact, append, lastChunk, metadata);
            return ValidationUtils.validateAndThrow(event);
        }
    }
}
