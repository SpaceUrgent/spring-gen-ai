package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record ImageSpec(
        @JsonPropertyDescription("Short title of the illustration")
        String title,
        @JsonPropertyDescription("One of: landscape, portrait, square")
        String orientation,
        SceneDetails scene,
        @JsonPropertyDescription("Every character visible in the image")
        List<CharacterDetails> characters,
        EnvironmentDetails environment,
        Composition composition,
        StyleDetails style,
        @JsonPropertyDescription("Single dense paragraph for an image generation model combining all visual details: subjects, action, setting, lighting, composition, style")
        String prompt,
        @JsonPropertyDescription("Comma separated things the image must NOT contain: anachronisms, details contradicting the book, rendering artifacts")
        String negativePrompt,
        @JsonPropertyDescription("Visual details that were invented because the book facts do not state them")
        List<String> assumptions
) {
}
