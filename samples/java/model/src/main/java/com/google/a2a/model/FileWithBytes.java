package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.Base64;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FileWithBytes(
    /** Optional name for the file */
    @JsonProperty("name")
    String name,

    /** Optional mimeType for the file */
    @JsonProperty("mimeType")
    String mimeType,

    /** base64 encoded content of the file*/
    @JsonProperty("bytes")
    @NotBlank(message = "File bytes cannot be blank")
    String bytes
) implements FileContent {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String mimeType = "application/octet-stream"; // default
        private String bytes;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType != null ? mimeType : "application/octet-stream";
            return this;
        }

        public Builder bytes(String bytes) {
            this.bytes = bytes;
            return this;
        }

        public Builder bytesFromByteArray(byte[] byteArray) {
            this.bytes = Base64.getEncoder().encodeToString(byteArray);
            return this;
        }

        public FileWithBytes build() {
            // Validate base64 format if bytes is not null or empty
            if (bytes != null && !bytes.trim().isEmpty()) {
                try {
                    Base64.getDecoder().decode(bytes);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("File bytes must be valid base64 encoded content");
                }
            }

            FileWithBytes file = new FileWithBytes(name, mimeType, bytes);
            return ValidationUtils.validateAndThrow(file);
        }
    }
}
