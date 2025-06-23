package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = APIKeySecurityScheme.class, name = "apiKey"),
    @JsonSubTypes.Type(value = HTTPAuthSecurityScheme.class, name = "http"),
    @JsonSubTypes.Type(value = OAuth2SecurityScheme.class, name = "oauth2"),
    @JsonSubTypes.Type(value = OpenIdConnectSecurityScheme.class, name = "openIdConnect")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface SecurityScheme
    permits APIKeySecurityScheme, HTTPAuthSecurityScheme, OAuth2SecurityScheme, OpenIdConnectSecurityScheme {
}
