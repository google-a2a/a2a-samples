package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Artifact(
    /** Unique identifier for the artifact. */
    @JsonProperty("artifactId")
    @NotBlank(message = "Artifact ID cannot be blank")
    String artifactId,

    /** Optional name for the artifact. */
    @JsonProperty("name")
    String name,

    /** Optional description for the artifact. */
    @JsonProperty("description")
    String description,

    /** Artifact parts. */
    @JsonProperty("parts")
    @NotNull(message = "Artifact parts cannot be null")
    @NotEmpty(message = "Artifact must have at least one part")
    @Valid
    List<Part> parts,

    /** Extension metadata. */
    @JsonProperty("metadata")
    Map<String, Object> metadata,

    /** The URIs of extensions that are present or contributed to this Artifact. */
    @JsonProperty("extensions")
    List<String> extensions
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String artifactId;
        private String name;
        private String description;
        private List<Part> parts = List.of(); // default empty
        private Map<String, Object> metadata = Map.of(); // default empty
        private List<String> extensions = List.of(); // default empty

        public Builder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
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

        public Artifact build() {
            Artifact artifact = new Artifact(artifactId, name, description, parts, metadata, extensions);
            return ValidationUtils.validateAndThrow(artifact);
        }
    }
}
