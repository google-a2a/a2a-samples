package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;

/**
 * Configuration for setting up push notifications for task updates.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PushNotificationConfig(
    /** Push Notification ID - created by server to support multiple callbacks */
    @JsonProperty("id")
    String id,

    /** URL for sending the push notifications. */
    @JsonProperty("url")
    @NotBlank(message = "Push notification URL cannot be blank")
    @Pattern(regexp = "^https?://.*", message = "Push notification URL must be a valid HTTP/HTTPS URL")
    String url,

    /** Token unique to this task/session. */
    @JsonProperty("token")
    String token,

    @JsonProperty("authentication")
    @Valid
    PushNotificationAuthenticationInfo authentication
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String url;
        private String token;
        private PushNotificationAuthenticationInfo authentication;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder authentication(PushNotificationAuthenticationInfo authentication) {
            this.authentication = authentication;
            return this;
        }

        public PushNotificationConfig build() {
            PushNotificationConfig config = new PushNotificationConfig(id, url, token, authentication);
            return ValidationUtils.validateAndThrow(config);
        }
    }
}
