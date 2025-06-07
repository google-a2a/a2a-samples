package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

/**
 * Represents a text segment within parts.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TextPart(
    /** Part type - text for TextParts*/
    @JsonProperty("kind")
    @Pattern(regexp = "^text$", message = "Kind must be 'text' for TextPart")
    String kind,

    /** Text content */
    @JsonProperty("text")
    @NotBlank(message = "Text content cannot be blank")
    String text,

    /** Optional metadata associated with the part. */
    @JsonProperty("metadata")
    Map<String, Object> metadata
) implements Part {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String kind = "text"; // default
        private String text;
        private Map<String, Object> metadata = Map.of(); // default empty

        public Builder kind(String kind) {
            this.kind = kind != null ? kind : "text";
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }

        public TextPart build() {
            TextPart part = new TextPart(kind, text, metadata);
            return ValidationUtils.validateAndThrow(part);
        }
    }
}
