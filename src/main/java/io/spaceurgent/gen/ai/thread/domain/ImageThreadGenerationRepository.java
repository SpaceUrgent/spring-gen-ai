package io.spaceurgent.gen.ai.thread.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageThreadGenerationRepository extends JpaRepository<ImageGenMetadata, UUID> {

    List<ImageGenMetadata> findAllByThreadIdOrderByCreatedAt(UUID threadId);

    boolean existsByThreadId(UUID threadId);
}
