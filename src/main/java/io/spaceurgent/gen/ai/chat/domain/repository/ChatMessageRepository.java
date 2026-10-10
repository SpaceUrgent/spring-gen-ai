package io.spaceurgent.gen.ai.chat.domain.repository;

import io.spaceurgent.gen.ai.chat.domain.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findAllByChatIdOrderByCreatedAt(UUID chatId);
}
