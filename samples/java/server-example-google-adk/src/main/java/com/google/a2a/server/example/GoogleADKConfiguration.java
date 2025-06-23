package com.google.a2a.server.example;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Google ADK services
 */
@Configuration
@ConfigurationProperties(prefix = "google.cloud.ai")
public class GoogleADKConfiguration {

    private String model = "gemini-2.0-flash";

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
