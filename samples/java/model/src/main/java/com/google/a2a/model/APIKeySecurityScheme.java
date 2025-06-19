package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record APIKeySecurityScheme(
    /** Security scheme type. */
    @JsonProperty("type") String type,
    /** Description of this security scheme. */
    @JsonProperty("description") String description,
    /** The name of the header, query or cookie parameter. */
    @JsonProperty("name") String name,
    /** The location of the API key. */
    @JsonProperty("in") String in
) implements SecurityScheme {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type = "apiKey"; // default
        private String description;
        private String name;
        private String in;

        public Builder type(String type) {
            this.type = type != null ? type : "apiKey";
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder in(String in) {
            this.in = in;
            return this;
        }

        public APIKeySecurityScheme build() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("API key name cannot be blank");
            }
            if (in == null || in.trim().isEmpty()) {
                throw new IllegalArgumentException("API key location cannot be blank");
            }
            if (!java.util.List.of("query", "header", "cookie").contains(in)) {
                throw new IllegalArgumentException("API key location must be 'query', 'header' or 'cookie'");
            }

            return new APIKeySecurityScheme(type, description, name, in);
        }
    }
}
