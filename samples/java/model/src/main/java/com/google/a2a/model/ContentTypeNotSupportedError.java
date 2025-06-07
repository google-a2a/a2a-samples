package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentTypeNotSupportedError(
    /** A Number that indicates the error type that occurred. */
    @JsonProperty("code") Integer code,
    /**
     * @default Incompatible content types
     */
    @JsonProperty("message") String message,
    @JsonProperty("data") Object data
) implements A2AError {}
