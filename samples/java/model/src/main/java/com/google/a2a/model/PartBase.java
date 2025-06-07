package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PartBase(
    /** Optional metadata associated with the part. */
    @JsonProperty("metadata") Map<String, Object> metadata
) {}
