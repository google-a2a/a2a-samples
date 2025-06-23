package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response object for streaming message events (SSE)
 * This is the structure of the JSON object found in the data field of each Server-Sent Event
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendStreamingMessageResponse(
    /**
     * JSON-RPC version string. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,

    /**
     * Matches the id from the originating message/stream or tasks/resubscribe request.
     */
    @JsonProperty("id") Object id,

    /**
     * The event payload. Can be Message, Task, TaskStatusUpdateEvent, or TaskArtifactUpdateEvent
     */
    @JsonProperty("result") Object result,

    /**
     * Error object if the streaming response contains an error
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

        public SendStreamingMessageResponse build() {
            return new SendStreamingMessageResponse(jsonrpc, id, result, error);
        }
    }
}
