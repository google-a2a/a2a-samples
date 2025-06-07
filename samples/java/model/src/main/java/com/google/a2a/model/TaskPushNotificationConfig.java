package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskPushNotificationConfig(
    /** Task id. */
    @JsonProperty("taskId")
    @NotBlank(message = "Task ID cannot be blank")
    String taskId,

    /** Push notification configuration. */
    @JsonProperty("pushNotificationConfig")
    @NotNull(message = "Push notification config cannot be null")
    @Valid
    PushNotificationConfig pushNotificationConfig
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String taskId;
        private PushNotificationConfig pushNotificationConfig;

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder pushNotificationConfig(PushNotificationConfig pushNotificationConfig) {
            this.pushNotificationConfig = pushNotificationConfig;
            return this;
        }

        public TaskPushNotificationConfig build() {
            TaskPushNotificationConfig config = new TaskPushNotificationConfig(taskId, pushNotificationConfig);
            return ValidationUtils.validateAndThrow(config);
        }
    }
}
