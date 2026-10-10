package io.spaceurgent.gen.ai.image.domain;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "images")
public class Image {

    @Id
    private UUID id;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    @Lob @Basic
    @Column
    private byte[] content;
}
