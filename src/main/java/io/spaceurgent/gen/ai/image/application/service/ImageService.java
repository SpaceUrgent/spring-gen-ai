package io.spaceurgent.gen.ai.image.application.service;

import io.spaceurgent.gen.ai.image.domain.Image;
import io.spaceurgent.gen.ai.image.domain.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;

    public Resource getImageContent(UUID id) {
        Image image = imageRepository.findById(id).orElseThrow(() -> {
            log.info("Image not found with id {}", id);
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image not found");
        });
        return new ByteArrayResource(image.getContent());
    }
}
