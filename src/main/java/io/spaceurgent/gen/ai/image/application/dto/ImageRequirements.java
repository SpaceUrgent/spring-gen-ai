package io.spaceurgent.gen.ai.image.application.dto;

import org.jspecify.annotations.Nullable;

public record ImageRequirements(@Nullable StyleDetails style, Orientation orientation, @Nullable String negativePrompt) {
}
