package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record Composition(
        @JsonPropertyDescription("e.g. close-up, medium shot, wide shot, establishing shot")
        String shotType,
        @JsonPropertyDescription("e.g. eye level, low angle, high angle, bird's-eye view")
        String cameraAngle,
        @JsonPropertyDescription("Main subject the viewer's eye should land on")
        String focalPoint,
        String foreground,
        String background
) {
}
