package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JSONRPCMessage(
    /**
     * Specifies the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    @JsonProperty("jsonrpc") String jsonrpc,
    /**
     * An identifier established by the Client that MUST contain a String, Number.
     * Numbers SHOULD NOT contain fractional parts.
     * @nullable true
     */
    @JsonProperty("id") Object id
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Object id;

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public JSONRPCMessage build() {
            return new JSONRPCMessage("2.0", id);
        }
    }
}
