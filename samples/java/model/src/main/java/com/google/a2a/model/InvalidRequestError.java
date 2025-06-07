package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InvalidRequestError(
    /** A Number that indicates the error type that occurred. */
    @JsonProperty("code") Integer code,
    /**
     * @default Request payload validation error
     */
    @JsonProperty("message") String message,
    @JsonProperty("data") Object data
) implements A2AError {}
