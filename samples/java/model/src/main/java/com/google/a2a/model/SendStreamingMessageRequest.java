package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC request model for the 'message/stream' method.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendStreamingMessageRequest(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    @JsonProperty("id") Object id,
    @JsonProperty("method") String method,
    @JsonProperty("params") MessageSendParams params
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jsonrpc = "2.0"; // default
        private Object id;
        private String method = "message/stream"; // default
        private MessageSendParams params;

        public Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc != null ? jsonrpc : "2.0";
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder method(String method) {
            this.method = method != null ? method : "message/stream";
            return this;
        }

        public Builder params(MessageSendParams params) {
            this.params = params;
            return this;
        }

        public SendStreamingMessageRequest build() {
            if (params == null) {
                throw new IllegalArgumentException("Message send params cannot be null");
            }

            return new SendStreamingMessageRequest(jsonrpc, id, method, params);
        }
    }
}
