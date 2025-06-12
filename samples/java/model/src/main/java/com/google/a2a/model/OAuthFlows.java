package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OAuthFlows(
    /** Configuration for the OAuth Authorization Code flow. Previously called accessCode in OpenAPI 2.0. */
    @JsonProperty("authorizationCode") AuthorizationCodeOAuthFlow authorizationCode,
    /** Configuration for the OAuth Client Credentials flow. Previously called application in OpenAPI 2.0 */
    @JsonProperty("clientCredentials") ClientCredentialsOAuthFlow clientCredentials,
    /** Configuration for the OAuth Implicit flow */
    @JsonProperty("implicit") ImplicitOAuthFlow implicit,
    /** Configuration for the OAuth Resource Owner Password flow */
    @JsonProperty("password") PasswordOAuthFlow password
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AuthorizationCodeOAuthFlow authorizationCode;
        private ImplicitOAuthFlow implicit;
        private PasswordOAuthFlow password;
        private ClientCredentialsOAuthFlow clientCredentials;

        public Builder authorizationCode(AuthorizationCodeOAuthFlow authorizationCode) {
            this.authorizationCode = authorizationCode;
            return this;
        }

        public Builder implicit(ImplicitOAuthFlow implicit) {
            this.implicit = implicit;
            return this;
        }

        public Builder password(PasswordOAuthFlow password) {
            this.password = password;
            return this;
        }

        public Builder clientCredentials(ClientCredentialsOAuthFlow clientCredentials) {
            this.clientCredentials = clientCredentials;
            return this;
        }

        public OAuthFlows build() {
            // At least one flow must be defined
            if (authorizationCode == null && implicit == null && password == null && clientCredentials == null) {
                throw new IllegalArgumentException("At least one OAuth flow must be defined");
            }

            return new OAuthFlows(authorizationCode, clientCredentials, implicit, password);
        }
    }
}
