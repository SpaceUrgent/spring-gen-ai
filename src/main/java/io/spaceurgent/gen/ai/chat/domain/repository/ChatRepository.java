package io.spaceurgent.gen.ai.chat.domain.repository;

import io.spaceurgent.gen.ai.chat.domain.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {

    List<Chat> findAllByOrderByCreatedAtDesc();
}
