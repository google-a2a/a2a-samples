package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC request model for the 'tasks/pushNotificationConfig/set' method.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SetTaskPushNotificationConfigRequest(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    @JsonProperty("id") Object id,
    /** A String containing the name of the method to be invoked. */
    @JsonProperty("method") String method,
    /** A Structured value that holds the parameter values to be used during the invocation of the method. */
    @JsonProperty("params") TaskPushNotificationConfig params
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String jsonrpc = "2.0"; // default
        private Object id;
        private String method = "tasks/pushNotificationConfig/set"; // default
        private TaskPushNotificationConfig params;

        public Builder jsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc != null ? jsonrpc : "2.0";
            return this;
        }

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder method(String method) {
            this.method = method != null ? method : "tasks/pushNotificationConfig/set";
            return this;
        }

        public Builder params(TaskPushNotificationConfig params) {
            this.params = params;
            return this;
        }

        public SetTaskPushNotificationConfigRequest build() {
            if (params == null) {
                throw new IllegalArgumentException("Push notification config params cannot be null");
            }

            return new SetTaskPushNotificationConfigRequest(jsonrpc, id, method, params);
        }
    }
}
