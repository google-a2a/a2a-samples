package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PasswordOAuthFlow(
    /**
     * The token URL to be used for this flow. This MUST be in the form of a URL. The OAuth2 standard
     * requires the use of TLS.
     */
    @JsonProperty("tokenUrl") String tokenUrl,
    /**
     * The URL to be used for obtaining refresh tokens. This MUST be in the form of a URL. The OAuth2
     * standard requires the use of TLS.
     */
    @JsonProperty("refreshUrl") String refreshUrl,
    /**
     * The available scopes for the OAuth2 security scheme. A map between the scope name and a short
     * description for it. The map MAY be empty.
     */
    @JsonProperty("scopes") Map<String, String> scopes
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String tokenUrl;
        private String refreshUrl;
        private Map<String, String> scopes = Map.of(); // default empty

        public Builder tokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
            return this;
        }

        public Builder refreshUrl(String refreshUrl) {
            this.refreshUrl = refreshUrl;
            return this;
        }

        public Builder scopes(Map<String, String> scopes) {
            this.scopes = scopes != null ? scopes : Map.of();
            return this;
        }

        public PasswordOAuthFlow build() {
            if (tokenUrl == null || tokenUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("Token URL cannot be blank");
            }
            if (!tokenUrl.matches("^https?://.*")) {
                throw new IllegalArgumentException("Token URL must be a valid HTTP/HTTPS URL");
            }

            return new PasswordOAuthFlow(tokenUrl, refreshUrl, scopes);
        }
    }
}
