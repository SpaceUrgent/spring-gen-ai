package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SceneDetails(
        @JsonPropertyDescription("What the image shows, in two or three sentences")
        String description,
        @JsonPropertyDescription("The exact moment of action being depicted")
        String action,
        @JsonPropertyDescription("Emotional tone of the scene")
        String mood
) {
}
