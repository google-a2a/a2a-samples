package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JSONRPCRequest(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    /**
     * An identifier established by the Client that MUST contain a String, Number.
     * Numbers SHOULD NOT contain fractional parts.
     * @nullable true
     */
    @JsonProperty("id") Object id,
    /**
     * A String containing the name of the method to be invoked.
     */
    @JsonProperty("method") String method,
    /**
     * A Structured value that holds the parameter values to be used during the invocation of the method.
     */
    @JsonProperty("params") Map<String, Object> params
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jsonrpc = "2.0"; // default
        private Object id;
        private String method;
        private Map<String, Object> params = Map.of(); // default empty

        public Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc != null ? jsonrpc : "2.0";
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder params(Map<String, Object> params) {
            this.params = params != null ? params : Map.of();
            return this;
        }

        public JSONRPCRequest build() {
            if (method == null || method.trim().isEmpty()) {
                throw new IllegalArgumentException("JSON-RPC method cannot be blank");
            }

            return new JSONRPCRequest(jsonrpc, id, method, params);
        }
    }
}
