package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JSONRPCError(
    /**
     * A Number that indicates the error type that occurred.
     */
    @JsonProperty("code") Integer code,
    /**
     * A String providing a short description of the error.
     */
    @JsonProperty("message") String message,
    /**
     * A Primitive or Structured value that contains additional information about the error.
     * This may be omitted.
     */
    @JsonProperty("data") Object data
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer code;
        private String message;
        private Object data;

        public Builder code(Integer code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public JSONRPCError build() {
            if (code == null) {
                throw new IllegalArgumentException("Error code cannot be null");
            }
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Error message cannot be blank");
            }

            return new JSONRPCError(code, message, data);
        }
    }
}
