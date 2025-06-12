package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "code"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = JSONParseError.class, name = "-32700"),
    @JsonSubTypes.Type(value = InvalidRequestError.class, name = "-32600"),
    @JsonSubTypes.Type(value = MethodNotFoundError.class, name = "-32601"),
    @JsonSubTypes.Type(value = InvalidParamsError.class, name = "-32602"),
    @JsonSubTypes.Type(value = InternalError.class, name = "-32603"),
    @JsonSubTypes.Type(value = TaskNotFoundError.class, name = "-32001"),
    @JsonSubTypes.Type(value = TaskNotCancelableError.class, name = "-32002"),
    @JsonSubTypes.Type(value = PushNotificationNotSupportedError.class, name = "-32003"),
    @JsonSubTypes.Type(value = UnsupportedOperationError.class, name = "-32004"),
    @JsonSubTypes.Type(value = ContentTypeNotSupportedError.class, name = "-32005"),
    @JsonSubTypes.Type(value = InvalidAgentResponseError.class, name = "-32006")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface A2AError
    permits JSONParseError, InvalidRequestError, MethodNotFoundError, InvalidParamsError, InternalError,
            TaskNotFoundError, TaskNotCancelableError, PushNotificationNotSupportedError,
            UnsupportedOperationError, ContentTypeNotSupportedError, InvalidAgentResponseError {
}
