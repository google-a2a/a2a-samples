package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FileBase(
    /** Optional name for the file */
    @JsonProperty("name") String name,
    /** Optional mimeType for the file */
    @JsonProperty("mimeType") String mimeType
) {}
