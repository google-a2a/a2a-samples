package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC request model for the 'tasks/cancel' method.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CancelTaskRequest(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    @JsonProperty("id") Object id,
    /** A String containing the name of the method to be invoked. */
    @JsonProperty("method") String method,
    /** Task ID parameter */
    @JsonProperty("params") String params
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jsonrpc = "2.0"; // default
        private Object id;
        private String method = "tasks/cancel"; // default
        private String params;

        public Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc != null ? jsonrpc : "2.0";
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder method(String method) {
            this.method = method != null ? method : "tasks/cancel";
            return this;
        }

        public Builder params(String params) {
            this.params = params;
            return this;
        }

        public CancelTaskRequest build() {
            if (params == null || params.trim().isEmpty()) {
                throw new IllegalArgumentException("Task ID params cannot be blank");
            }

            return new CancelTaskRequest(jsonrpc, id, method, params);
        }
    }
}
