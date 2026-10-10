package io.spaceurgent.gen.ai.chat.application.dto;

import io.spaceurgent.gen.ai.chat.domain.model.ChatMessageRole;

import java.time.Instant;

public record ChatMessageDto(
        ChatMessageRole role,
        String content,
        Instant createdAt
) {
}
