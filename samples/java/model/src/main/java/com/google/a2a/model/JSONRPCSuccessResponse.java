package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JSONRPCSuccessResponse(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    /**
     * @nullable true
     */
    @JsonProperty("id") Object id,
    /**
     * The result object on success
     */
    @JsonProperty("result") Object result
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jsonrpc = "2.0"; // default
        private Object id;
        private Object result;

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

        public JSONRPCSuccessResponse build() {
            return new JSONRPCSuccessResponse(jsonrpc, id, result);
        }
    }
}
