package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ImageSpecDraft(
        @JsonPropertyDescription("Short title of the illustration")
        String title,
        SceneDetails scene,
        @JsonPropertyDescription("Every character visible in the image")
        List<CharacterDetails> characters,
        EnvironmentDetails environment,
        Composition composition,
        @JsonPropertyDescription("Single dense paragraph for an image generation model combining all visual details: subjects, action, setting, lighting, composition and the required style. Must not mention anything the image has to avoid")
        String prompt,
        @JsonPropertyDescription("Visual details that were invented because the book facts do not state them")
        List<String> assumptions
) {
}
