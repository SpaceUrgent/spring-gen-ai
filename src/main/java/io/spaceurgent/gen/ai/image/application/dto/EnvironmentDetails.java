package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record EnvironmentDetails(
        @JsonPropertyDescription("Specific place, indoor or outdoor")
        String location,
        @JsonPropertyDescription("Historical period or technology level of the setting")
        String era,
        String timeOfDay,
        String weather,
        @JsonPropertyDescription("Light sources, direction, color temperature, contrast")
        String lighting,
        String terrain,
        String vegetation,
        @JsonPropertyDescription("Buildings, interiors, materials")
        String architecture,
        @JsonPropertyDescription("Notable objects present in the scene")
        List<String> props
) {
}
