package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JSONRPCResponse(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    /**
     * An identifier established by the Client that MUST contain a String, Number, or NULL value if included.
     */
    @JsonProperty("id") Object id,
    /**
     * The result object on success. This member is REQUIRED on success.
     * This member MUST NOT exist if there was an error invoking the method.
     */
    @JsonProperty("result") Object result,
    /**
     * The error object on failure. This member is REQUIRED on failure.
     * This member MUST NOT exist if there was no error triggered during invocation.
     */
    @JsonProperty("error") JSONRPCError error
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jsonrpc = "2.0"; // default
        private Object id;
        private Object result;
        private JSONRPCError error;

        public Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc != null ? jsonrpc : "2.0";
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder result(Object result) {
            this.result = result;
            return this;
        }

        public Builder error(JSONRPCError error) {
            this.error = error;
            return this;
        }

        public JSONRPCResponse build() {
            // Validate that either result or error is present, but not both
            if (result != null && error != null) {
                throw new IllegalArgumentException("Response cannot have both result and error");
            }
            if (result == null && error == null) {
                throw new IllegalArgumentException("Response must have either result or error");
            }

            return new JSONRPCResponse(jsonrpc, id, result, error);
        }
    }
}
