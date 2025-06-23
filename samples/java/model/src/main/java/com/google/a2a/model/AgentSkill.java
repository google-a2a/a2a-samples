package com.google.a2a.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Represents a unit of capability that an agent can perform.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentSkill(
    /** Unique identifier for the agent's skill. */
    @JsonProperty("id")
    @NotBlank(message = "Skill ID cannot be blank")
    String id,

    /** Human readable name of the skill. */
    @JsonProperty("name")
    @NotBlank(message = "Skill name cannot be blank")
    String name,

    /**
     * Description of the skill - will be used by the client or a human
     * as a hint to understand what the skill does.
     */
    @JsonProperty("description")
    @NotBlank(message = "Skill description cannot be blank")
    String description,

    /**
     * Set of tagwords describing classes of capabilities for this specific skill.
     * @example ["cooking", "customer support", "billing"]
     */
    @JsonProperty("tags")
    @NotNull(message = "Skill tags cannot be null")
    @NotEmpty(message = "Skill must have at least one tag")
    List<String> tags,

    /**
     * The set of example scenarios that the skill can perform.
     * Will be used by the client as a hint to understand how the skill can be used.
     * @example ["I need a recipe for bread"]
     */
    @JsonProperty("examples")
    List<String> examples, // example prompts for tasks

    /**
     * The set of interaction modes that the skill supports
     * (if different than the default).
     * Supported media types for input.
     */
    @JsonProperty("inputModes")
    List<String> inputModes,

    /** Supported media types for output. */
    @JsonProperty("outputModes")
    List<String> outputModes
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String description;
        private List<String> tags = List.of(); // default empty list
        private List<String> examples = List.of(); // default empty list
        private List<String> inputModes = List.of(); // default empty list
        private List<String> outputModes = List.of(); // default empty list

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags != null ? tags : List.of();
            return this;
        }

        public Builder examples(List<String> examples) {
            this.examples = examples != null ? examples : List.of();
            return this;
        }

        public Builder inputModes(List<String> inputModes) {
            this.inputModes = inputModes != null ? inputModes : List.of();
            return this;
        }

        public Builder outputModes(List<String> outputModes) {
            this.outputModes = outputModes != null ? outputModes : List.of();
            return this;
        }

        public AgentSkill build() {
            AgentSkill skill = new AgentSkill(id, name, description, tags, examples, inputModes, outputModes);
            return ValidationUtils.validateAndThrow(skill);
        }
    }
}
