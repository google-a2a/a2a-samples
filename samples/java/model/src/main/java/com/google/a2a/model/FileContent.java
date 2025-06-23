package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.DEDUCTION
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = FileWithBytes.class),
    @JsonSubTypes.Type(value = FileWithUri.class)
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface FileContent
    permits FileWithBytes, FileWithUri {
}
