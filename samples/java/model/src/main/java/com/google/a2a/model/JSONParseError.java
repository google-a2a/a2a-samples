package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JSONParseError(
    @JsonProperty("code") Integer code,
    /**
     * @default Invalid JSON payload
     */
    @JsonProperty("message") String message,
    @JsonProperty("data") Object data
) implements A2AError {}
