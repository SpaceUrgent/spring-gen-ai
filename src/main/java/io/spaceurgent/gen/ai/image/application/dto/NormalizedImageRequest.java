package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record NormalizedImageRequest(
        @JsonPropertyDescription("The request translated to English")
        String prompt,
        @JsonPropertyDescription("The required style in English, rewritten in precise art terminology. Fields that are not given stay empty")
        StyleDetails style,
        @JsonPropertyDescription("Comma separated list of things the image must not contain, in English. Empty if not given")
        String negativePrompt
) {
}
