package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record SceneIntent(
        @JsonPropertyDescription("The episode, moment or place to depict, phrased as a search query, without style, color, camera or image format words. Empty if only characters are requested")
        String sceneQuery,
        @JsonPropertyDescription("Names of characters to depict, exactly as written in the request")
        List<String> characters
) {
}
