package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FilePart(
    /** Part type - file for FileParts */
    @JsonProperty("kind")
    @Pattern(regexp = "^file$", message = "Kind must be 'file' for FilePart")
    String kind,

    /** File content either as url or bytes */
    @JsonProperty("file")
    @NotNull(message = "File content cannot be null")
    @Valid
    FileContent file,

    /** Optional metadata associated with the part. */
    @JsonProperty("metadata")
    Map<String, Object> metadata
) implements Part {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String kind = "file"; // default
        private FileContent file;
        private Map<String, Object> metadata = Map.of(); // default empty

        public Builder kind(String kind) {
            this.kind = kind != null ? kind : "file";
            return this;
        }

        public Builder file(FileContent file) {
            this.file = file;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }

        public FilePart build() {
            FilePart part = new FilePart(kind, file, metadata);
            return ValidationUtils.validateAndThrow(part);
        }
    }
}
