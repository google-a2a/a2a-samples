package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenIdConnectSecurityScheme(
    /** Security scheme type. */
    @JsonProperty("type") String type,
    /** Description of this security scheme. */
    @JsonProperty("description") String description,
    /** OpenId Connect URL to discover OAuth2 configuration values. */
    @JsonProperty("openIdConnectUrl") String openIdConnectUrl
) implements SecurityScheme {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type = "openIdConnect"; // default
        private String description;
        private String openIdConnectUrl;

        public Builder type(String type) {
            this.type = type != null ? type : "openIdConnect";
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder openIdConnectUrl(String openIdConnectUrl) {
            this.openIdConnectUrl = openIdConnectUrl;
            return this;
        }

        public OpenIdConnectSecurityScheme build() {
            if (openIdConnectUrl == null || openIdConnectUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("OpenID Connect URL cannot be blank");
            }
            if (!openIdConnectUrl.matches("^https?://.*")) {
                throw new IllegalArgumentException("OpenID Connect URL must be a valid HTTP/HTTPS URL");
            }

            return new OpenIdConnectSecurityScheme(type, description, openIdConnectUrl);
        }
    }
}
