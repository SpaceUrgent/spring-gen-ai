package io.spaceurgent.gen.ai.image.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ImageRequest {
        @NotNull
        private UUID bookId;
        private Orientation orientation = Orientation.LANDSCAPE;
        private StyleDetails style;
        @NotBlank
        private String prompt;
        private String negativePrompt;
}
