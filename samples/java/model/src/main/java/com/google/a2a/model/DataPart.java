package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DataPart(
    /** Part type - data for DataParts */
    @JsonProperty("kind")
    @Pattern(regexp = "^data$", message = "Kind must be 'data' for DataPart")
    String kind,

    /** Structured data content */
    @JsonProperty("data")
    @NotNull(message = "Data content cannot be null")
    @NotEmpty(message = "Data content cannot be empty")
    Map<String, Object> data,

    /** Optional metadata associated with the part. */
    @JsonProperty("metadata")
    Map<String, Object> metadata
) implements Part {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String kind = "data"; // default
        private Map<String, Object> data = Map.of(); // default empty
        private Map<String, Object> metadata = Map.of(); // default empty

        public Builder kind(String kind) {
            this.kind = kind != null ? kind : "data";
            return this;
        }

        public Builder data(Map<String, Object> data) {
            this.data = data != null ? data : Map.of();
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }

        public DataPart build() {
            DataPart part = new DataPart(kind, data, metadata);
            return ValidationUtils.validateAndThrow(part);
        }
    }
}
