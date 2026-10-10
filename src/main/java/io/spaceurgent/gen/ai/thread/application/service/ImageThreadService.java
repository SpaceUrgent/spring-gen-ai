package io.spaceurgent.gen.ai.thread.application.service;

import io.spaceurgent.gen.ai.book.domain.Book;
import io.spaceurgent.gen.ai.book.application.dto.BookRef;
import io.spaceurgent.gen.ai.book.application.service.BookService;
import io.spaceurgent.gen.ai.shared.exception.BadRequestException;
import io.spaceurgent.gen.ai.image.application.dto.ImageRef;
import io.spaceurgent.gen.ai.image.application.dto.ImageRequest;
import io.spaceurgent.gen.ai.image.application.dto.ImageSpec;
import io.spaceurgent.gen.ai.image.domain.ImageRepository;
import io.spaceurgent.gen.ai.image.application.service.IllustrationSpecService;
import io.spaceurgent.gen.ai.image.application.service.ImageGenerationService;
import io.spaceurgent.gen.ai.thread.domain.ImageThreadGenerationRepository;
import io.spaceurgent.gen.ai.thread.domain.ImageThreadRepository;
import io.spaceurgent.gen.ai.thread.application.dto.GenImageMetadataDto;
import io.spaceurgent.gen.ai.thread.application.dto.ImageThreadRequest;
import io.spaceurgent.gen.ai.thread.domain.GenImageRequest;
import io.spaceurgent.gen.ai.thread.application.dto.ImageThreadDetailsDto;
import io.spaceurgent.gen.ai.thread.application.dto.ImageThreadDto;
import io.spaceurgent.gen.ai.thread.domain.ImageGenMetadata;
import io.spaceurgent.gen.ai.thread.domain.ImageThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.openai.autoconfigure.OpenAiImageProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageThreadService {

    private final ImageThreadRepository threadRepository;
    private final ImageThreadGenerationRepository generationRepository;
    private final BookService bookService;
    private final IllustrationSpecService imageSpecService;
    private final ImageGenerationService imageGenerationService;
    private final OpenAiImageProperties imageProperties;
    private final ImageRepository imageRepository;

    public List<ImageThreadDto> getAll() {
        return threadRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(thread -> new ImageThreadDto(thread.getId(), thread.getTitle()))
                .toList();
    }

    public ImageThreadDetailsDto get(UUID id) {
        return toImageThreadDetailsDto(findImageThreadById(id));
    }

    @Transactional
    public ImageThreadDetailsDto create(ImageThreadRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Book book = bookService.findById(request.getBookId())
                .orElseThrow(() -> new BadRequestException("Book not found"));
        ImageThread thread = threadRepository.save(ImageThread.builder()
                .id(UUID.randomUUID())
                .title(book.getTitle())
                .book(book)
                .request(request.getGenImageRequest())
                .build());
        return generate(thread);
    }

    @Transactional
    public ImageThreadDetailsDto update(UUID id, GenImageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        ImageThread thread = findImageThreadById(id);
        thread.setRequest(request);
        return generate(threadRepository.save(thread));
    }

    private ImageThreadDetailsDto generate(ImageThread thread) {
        GenImageRequest request = thread.getRequest();
        ImageSpec spec = imageSpecService.create(toImageRequest(thread.getBook().getId(), request));
        ImageRef image = imageGenerationService.generate(spec);

        boolean firstGeneration = !generationRepository.existsByThreadId(thread.getId());
        generationRepository.save(ImageGenMetadata.builder()
                .id(UUID.randomUUID())
                .thread(thread)
                .image(imageRepository.getReferenceById(image.id()))
                .model(imageProperties.getModel())
                .request(request)
                .imageSpec(spec)
                .build());
        log.info("Image {} generated for thread {}", image.id(), thread.getId());

        if (firstGeneration && StringUtils.hasText(spec.title())) {
            thread.setTitle(spec.title());
            thread = threadRepository.save(thread);
        }
        return toImageThreadDetailsDto(thread);
    }

    private ImageThread findImageThreadById(UUID id) {
        return threadRepository.findById(id).orElseThrow(() -> new BadRequestException("Thread not found"));
    }

    private ImageThreadDetailsDto toImageThreadDetailsDto(ImageThread thread) {
        List<GenImageMetadataDto> results = generationRepository.findAllByThreadIdOrderByCreatedAt(thread.getId()).stream()
                .map(ImageThreadService::toGenImageMetadataDto)
                .toList();
        return new ImageThreadDetailsDto(
                thread.getId(),
                thread.getTitle(),
                new BookRef(thread.getBook().getId().toString(), thread.getBook().getTitle()),
                thread.getRequest(),
                results);
    }

    private static GenImageMetadataDto toGenImageMetadataDto(ImageGenMetadata generation) {
        GenImageMetadataDto metadata = new GenImageMetadataDto();
        metadata.setImageId(generation.getImage().getId());
        metadata.setCreationAt(generation.getCreatedAt());
        metadata.setModel(generation.getModel());
        metadata.setUserRequest(generation.getRequest());
        metadata.setImageSpec(generation.getImageSpec());
        return metadata;
    }

    private static ImageRequest toImageRequest(UUID bookId, GenImageRequest request) {
        ImageRequest imageRequest = new ImageRequest();
        imageRequest.setBookId(bookId);
        imageRequest.setOrientation(request.getOrientation());
        imageRequest.setStyle(request.getStyle());
        imageRequest.setPrompt(request.getPrompt());
        imageRequest.setNegativePrompt(request.getNegativePrompt());
        return imageRequest;
    }
}
