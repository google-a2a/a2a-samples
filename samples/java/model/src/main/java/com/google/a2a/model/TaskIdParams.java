package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskIdParams(
    /** Task id. */
    @JsonProperty("id")
    @NotBlank(message = "Task ID cannot be blank")
    String id,

    /** Extension metadata. */
    @JsonProperty("metadata")
    Map<String, Object> metadata
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Map<String, Object> metadata = Map.of(); // default empty

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }

        public TaskIdParams build() {
            TaskIdParams params = new TaskIdParams(id, metadata);
            return ValidationUtils.validateAndThrow(params);
        }
    }
}
