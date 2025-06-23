package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC success response model for the 'message/stream' method.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendStreamingMessageSuccessResponse(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    @JsonProperty("id") Object id,
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

        public SendStreamingMessageSuccessResponse build() {
            return new SendStreamingMessageSuccessResponse(jsonrpc, id, result);
        }
    }
}
