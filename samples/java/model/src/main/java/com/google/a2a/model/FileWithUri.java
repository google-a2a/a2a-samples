package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FileWithUri(
    /** Optional name for the file */
    @JsonProperty("name")
    String name,

    /** Optional mimeType for the file */
    @JsonProperty("mimeType")
    String mimeType,

    /** URL for the File content */
    @JsonProperty("uri")
    @NotBlank(message = "File URI cannot be blank")
    @Pattern(regexp = "^https?://.*", message = "File URI must be a valid HTTP/HTTPS URL")
    String uri
) implements FileContent {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String mimeType = "application/octet-stream"; // default
        private String uri;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType != null ? mimeType : "application/octet-stream";
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public FileWithUri build() {
            FileWithUri file = new FileWithUri(name, mimeType, uri);
            return ValidationUtils.validateAndThrow(file);
        }
    }
}
