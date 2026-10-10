package io.spaceurgent.gen.ai.image.application.service;

import io.spaceurgent.gen.ai.image.domain.ImageRepository;
import io.spaceurgent.gen.ai.image.application.dto.ImageRef;
import io.spaceurgent.gen.ai.image.application.dto.ImageSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    private static final String PROMPT = """
            Create an image from the JSON image specification below.
            "prompt" is the main description of the image; every other field adds visual detail and must be respected.
            Draw every character listed in "characters", with their described appearance, pose and expression.
            Follow "environment", "composition" and "style" exactly.
            Do not include anything listed in "negativePrompt".
            Do not draw the JSON itself, its field names or any of its text in the image.

            Image specification:
            %s
            """;

    private final OpenAiImageModel imageModel;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;
    private final ImageRepository imageRepository;

    public ImageRef generate(ImageSpec spec) {
        if (spec == null || spec.prompt() == null || spec.prompt().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illustration spec must contain a prompt");
        }

        OpenAiImageOptions.Builder options = OpenAiImageOptions.builder()
                .n(1)
                .size(size(spec.orientation()));

        ImageResponse response = imageModel.call(new ImagePrompt(buildPrompt(spec), options.build()));
        ImageGeneration generation = response.getResult();
        if (generation == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Image model returned no image");
        }
        var image = io.spaceurgent.gen.ai.image.domain.Image.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .content(content(generation.getOutput()))
                .build();
        image = imageRepository.save(image);
        return new ImageRef(image.getId());
    }

    private String buildPrompt(ImageSpec spec) {
        return PROMPT.formatted(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec));
    }

    private static String size(String orientation) {
        String normalized = orientation == null ? "" : orientation.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "landscape" -> "1536x1024";
            case "portrait" -> "1024x1536";
            default -> "1024x1024";
        };
    }

    private byte[] content(Image image) {
        if (image == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image model returned no image");
        }
        if (image.getB64Json() != null) {
            return Base64.getDecoder().decode(image.getB64Json());
        }
        if (image.getUrl() != null) {
            byte[] bytes = restClient.get().uri(URI.create(image.getUrl())).retrieve().body(byte[].class);
            if (bytes != null && bytes.length > 0) {
                return bytes;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image model returned neither image data nor URL");
    }
}
