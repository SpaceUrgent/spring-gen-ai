package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record SceneCharacters(
        @JsonPropertyDescription("Names of the characters who are present in the requested moment")
        List<String> names
) {
}
