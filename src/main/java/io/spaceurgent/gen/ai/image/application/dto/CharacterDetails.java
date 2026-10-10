package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CharacterDetails(
        String name,
        @JsonPropertyDescription("What the character does or represents in this scene")
        String role,
        @JsonPropertyDescription("Apparent age or age range")
        String age,
        @JsonPropertyDescription("Height, body type, posture")
        String build,
        @JsonPropertyDescription("Face shape, skin, eyes, facial hair, scars")
        String face,
        @JsonPropertyDescription("Hair color, length and style")
        String hair,
        @JsonPropertyDescription("Facial expression and emotion in this moment")
        String expression,
        @JsonPropertyDescription("Body pose, gesture and position in the frame")
        String pose,
        @JsonPropertyDescription("Garments with colors, materials and condition")
        List<String> clothing,
        @JsonPropertyDescription("Items held or carried: weapons, devices, accessories")
        List<String> equipment,
        @JsonPropertyDescription("Other distinguishing visual features")
        List<String> distinguishingFeatures
) {
}
