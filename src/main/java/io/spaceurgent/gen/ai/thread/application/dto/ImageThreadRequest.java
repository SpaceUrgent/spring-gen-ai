package io.spaceurgent.gen.ai.thread.application.dto;

import io.spaceurgent.gen.ai.thread.domain.GenImageRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ImageThreadRequest {
    @NotNull
    private UUID bookId;
    @NotNull
    @Valid
    private GenImageRequest genImageRequest;
}
