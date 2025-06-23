package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "kind"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextPart.class, name = "text"),
    @JsonSubTypes.Type(value = FilePart.class, name = "file"),
    @JsonSubTypes.Type(value = DataPart.class, name = "data")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface Part
    permits TextPart, FilePart, DataPart {
}
