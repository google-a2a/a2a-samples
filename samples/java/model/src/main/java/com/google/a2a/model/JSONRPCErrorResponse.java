package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JSONRPCErrorResponse(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    /**
     * @nullable true
     */
    @JsonProperty("id") Object id,
    @JsonProperty("error") Object error
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jsonrpc = "2.0"; // default
        private Object id;
        private Object error;

        public Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc != null ? jsonrpc : "2.0";
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder error(Object error) {
            this.error = error;
            return this;
        }

        public JSONRPCErrorResponse build() {
            if (error == null) {
                throw new IllegalArgumentException("Error cannot be null");
            }

            return new JSONRPCErrorResponse(jsonrpc, id, error);
        }
    }
}
