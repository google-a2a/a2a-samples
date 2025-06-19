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

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Message(
    /** Message sender's role */
    @JsonProperty("role")
    @NotBlank(message = "Message role cannot be blank")
    @Pattern(regexp = "^(user|agent)$", message = "Message role must be 'user' or 'agent'")
    String role,

    /** Message content */
    @JsonProperty("parts")
    @NotNull(message = "Message parts cannot be null")
    @NotEmpty(message = "Message must have at least one part")
    @Valid
    List<Part> parts,

    /** Extension metadata. */
    @JsonProperty("metadata")
    Map<String, Object> metadata,

    /** The URIs of extensions that are present or contributed to this Message. */
    @JsonProperty("extensions")
    List<String> extensions,

    /** List of tasks referenced as context by this message.*/
    @JsonProperty("referenceTaskIds")
    List<String> referenceTaskIds,

    /** Identifier created by the message creator*/
    @JsonProperty("messageId")
    @NotBlank(message = "Message ID cannot be blank")
    String messageId,

    /** Identifier of task the message is related to */
    @JsonProperty("taskId")
    String taskId,

    /** The context the message is associated with */
    @JsonProperty("contextId")
    String contextId,

    /** Event type */
    @JsonProperty("kind")
    @Pattern(regexp = "^message$", message = "Kind must be 'message'")
    String kind
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String role;
        private List<Part> parts = List.of(); // default empty
        private Map<String, Object> metadata = Map.of(); // default empty
        private List<String> extensions = List.of(); // default empty
        private List<String> referenceTaskIds = List.of(); // default empty
        private String messageId;
        private String taskId;
        private String contextId;
        private String kind = "message"; // default

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder parts(List<Part> parts) {
            this.parts = parts != null ? parts : List.of();
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }

        public Builder extensions(List<String> extensions) {
            this.extensions = extensions != null ? extensions : List.of();
            return this;
        }

        public Builder referenceTaskIds(List<String> referenceTaskIds) {
            this.referenceTaskIds = referenceTaskIds != null ? referenceTaskIds : List.of();
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public Builder kind(String kind) {
            this.kind = kind != null ? kind : "message";
            return this;
        }

        public Message build() {
            Message message = new Message(role, parts, metadata, extensions, referenceTaskIds, messageId, taskId, contextId, kind);
            return ValidationUtils.validateAndThrow(message);
        }
    }
}
