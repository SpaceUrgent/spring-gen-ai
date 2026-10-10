package io.spaceurgent.gen.ai.thread.application.dto;

import io.spaceurgent.gen.ai.book.application.dto.BookRef;
import io.spaceurgent.gen.ai.thread.domain.GenImageRequest;

import java.util.List;
import java.util.UUID;

public record ImageThreadDetailsDto(
        UUID id,
        String title,
        BookRef book,
        GenImageRequest userRequest,
        List<GenImageMetadataDto> generationResults
) {
}
