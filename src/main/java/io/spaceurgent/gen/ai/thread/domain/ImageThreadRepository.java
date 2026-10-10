package io.spaceurgent.gen.ai.thread.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageThreadRepository extends JpaRepository<ImageThread, UUID> {

    List<ImageThread> findAllByOrderByUpdatedAtDesc();
}
