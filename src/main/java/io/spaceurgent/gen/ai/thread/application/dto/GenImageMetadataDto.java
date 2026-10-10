package io.spaceurgent.gen.ai.thread.application.dto;

import io.spaceurgent.gen.ai.image.application.dto.ImageSpec;
import io.spaceurgent.gen.ai.thread.domain.GenImageRequest;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class GenImageMetadataDto {
    private UUID imageId;
    private Instant creationAt;
    private String model;
    private GenImageRequest userRequest;
    private ImageSpec imageSpec;
}
