package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record StyleDetails(
        @JsonPropertyDescription("Art style, e.g. realistic, noir comic, watercolor illustration")
        String style,
        @JsonPropertyDescription("Rendering medium, e.g. digital painting, oil on canvas, ink, 3D render")
        String medium,
        @JsonPropertyDescription("Dominant colors")
        String colorPalette,
        @JsonPropertyDescription("Level of detail, textures, brushwork")
        String detail
) {
}
