package io.spaceurgent.gen.ai.image.api;

import io.spaceurgent.gen.ai.image.application.dto.ImageRef;
import io.spaceurgent.gen.ai.image.application.dto.ImageRequest;
import io.spaceurgent.gen.ai.image.application.dto.ImageSpec;
import io.spaceurgent.gen.ai.image.application.service.IllustrationSpecService;
import io.spaceurgent.gen.ai.image.application.service.ImageGenerationService;
import io.spaceurgent.gen.ai.image.application.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final IllustrationSpecService imageSpecService;
    private final ImageGenerationService imageGenerationService;

    @GetMapping(
            value = "/{id}",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    public Resource getImage(@PathVariable UUID id) {
        return imageService.getImageContent(id);
    }

    @PostMapping(
            value = "/spec",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ImageSpec generateSpec(@RequestBody @Valid ImageRequest request) {
        return imageSpecService.create(request);
    }

    @PostMapping(
            value = "/spec/generate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ImageRef generate(@RequestBody ImageSpec spec) {
        return imageGenerationService.generate(spec);
    }

    @PostMapping(
            value = "/generate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ImageRef generate(@RequestBody @Valid ImageRequest request) {
        ImageSpec imageSpec = imageSpecService.create(request);
        return imageGenerationService.generate(imageSpec);
    }
}
