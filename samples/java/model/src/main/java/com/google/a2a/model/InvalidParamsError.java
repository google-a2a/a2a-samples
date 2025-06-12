package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InvalidParamsError(
    /** A Number that indicates the error type that occurred. */
    @JsonProperty("code") Integer code,
    /**
     * @default Invalid parameters
     */
    @JsonProperty("message") String message,
    @JsonProperty("data") Object data
) implements A2AError {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer code = -32602; // default for invalid params
        private String message = "Invalid parameters"; // default
        private Object data;

        public Builder code(Integer code) {
            this.code = code != null ? code : -32602;
            return this;
        }

        public Builder message(String message) {
            this.message = message != null ? message : "Invalid parameters";
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public InvalidParamsError build() {
            return new InvalidParamsError(code, message, data);
        }
    }
}
