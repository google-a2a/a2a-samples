package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC success response model for the 'tasks/pushNotificationConfig/set' method.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SetTaskPushNotificationConfigSuccessResponse(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    @JsonProperty("id") Object id,
    /** The result object on success. */
    @JsonProperty("result") TaskPushNotificationConfig result
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jsonrpc = "2.0"; // default
        private Object id;
        private TaskPushNotificationConfig result;

        public Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc != null ? jsonrpc : "2.0";
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder result(TaskPushNotificationConfig result) {
            this.result = result;
            return this;
        }

        public SetTaskPushNotificationConfigSuccessResponse build() {
            if (result == null) {
                throw new IllegalArgumentException("Push notification config result cannot be null");
            }

            return new SetTaskPushNotificationConfigSuccessResponse(jsonrpc, id, result);
        }
    }
}
