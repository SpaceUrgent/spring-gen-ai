package io.spaceurgent.gen.ai.chat.application.dto;

import java.util.List;
import java.util.UUID;

public record ChatDetailsDto(
        UUID id,
        String title,
        List<ChatMessageDto> messages
) {
}
