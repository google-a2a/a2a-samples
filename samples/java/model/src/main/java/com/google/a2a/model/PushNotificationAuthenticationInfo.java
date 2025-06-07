package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PushNotificationAuthenticationInfo(
    /** Supported authentication schemes - e.g. Basic, Bearer */
    @JsonProperty("schemes")
    @NotNull(message = "Authentication schemes cannot be null")
    @NotEmpty(message = "Must specify at least one authentication scheme")
    List<String> schemes,

    /** Optional credentials */
    @JsonProperty("credentials")
    String credentials
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> schemes = List.of(); // default empty
        private String credentials;

        public Builder schemes(List<String> schemes) {
            this.schemes = schemes != null ? schemes : List.of();
            return this;
        }

        public Builder credentials(String credentials) {
            this.credentials = credentials;
            return this;
        }

        public PushNotificationAuthenticationInfo build() {
            PushNotificationAuthenticationInfo auth = new PushNotificationAuthenticationInfo(schemes, credentials);
            return ValidationUtils.validateAndThrow(auth);
        }
    }
}
