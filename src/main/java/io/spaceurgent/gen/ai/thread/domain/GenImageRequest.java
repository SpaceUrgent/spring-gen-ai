package io.spaceurgent.gen.ai.thread.domain;

import io.spaceurgent.gen.ai.image.application.dto.Orientation;
import io.spaceurgent.gen.ai.image.application.dto.StyleDetails;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenImageRequest {
    private Orientation orientation = Orientation.LANDSCAPE;
    private StyleDetails style;
    @NotBlank
    private String prompt;
    private String negativePrompt;
}
