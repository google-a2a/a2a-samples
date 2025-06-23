package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC success response for the 'tasks/get' method.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetTaskSuccessResponse(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    @JsonProperty("id") Object id,
    /** The result object on success. */
    @JsonProperty("result") Task result
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jsonrpc = "2.0"; // default
        private Object id;
        private Task result;

        public Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc != null ? jsonrpc : "2.0";
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder result(Task result) {
            this.result = result;
            return this;
        }

        public GetTaskSuccessResponse build() {
            if (result == null) {
                throw new IllegalArgumentException("Task result cannot be null");
            }

            return new GetTaskSuccessResponse(jsonrpc, id, result);
        }
    }
}
