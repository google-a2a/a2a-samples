package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskQueryParams(
    /** Task id. */
    @JsonProperty("id")
    @NotBlank(message = "Task ID cannot be blank")
    String id,

    /** Extension metadata. */
    @JsonProperty("metadata")
    Map<String, Object> metadata,

    /** Number of recent messages to be retrieved. */
    @JsonProperty("historyLength")
    @Min(value = 0, message = "History length must be non-negative")
    Integer historyLength
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Map<String, Object> metadata = Map.of(); // default empty
        private Integer historyLength; // no default, optional field

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }

        public Builder historyLength(Integer historyLength) {
            this.historyLength = historyLength;
            return this;
        }

        public TaskQueryParams build() {
            TaskQueryParams params = new TaskQueryParams(id, metadata, historyLength);
            return ValidationUtils.validateAndThrow(params);
        }
    }
}
