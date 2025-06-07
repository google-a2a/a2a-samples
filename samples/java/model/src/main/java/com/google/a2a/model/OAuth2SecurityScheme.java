package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OAuth2SecurityScheme(
    /** Security scheme type. */
    @JsonProperty("type") String type,
    /** Description of this security scheme. */
    @JsonProperty("description") String description,
    /** An object containing configuration information for the flow types supported. */
    @JsonProperty("flows") OAuthFlows flows
) implements SecurityScheme {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type = "oauth2"; // default
        private String description;
        private OAuthFlows flows;

        public Builder type(String type) {
            this.type = type != null ? type : "oauth2";
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder flows(OAuthFlows flows) {
            this.flows = flows;
            return this;
        }

        public OAuth2SecurityScheme build() {
            if (flows == null) {
                throw new IllegalArgumentException("OAuth flows cannot be null");
            }

            return new OAuth2SecurityScheme(type, description, flows);
        }
    }
}
