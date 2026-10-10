package io.spaceurgent.gen.ai.thread.api;

import io.spaceurgent.gen.ai.thread.application.dto.ImageThreadRequest;
import io.spaceurgent.gen.ai.thread.application.service.ImageThreadService;
import io.spaceurgent.gen.ai.thread.domain.GenImageRequest;
import io.spaceurgent.gen.ai.thread.application.dto.ImageThreadDetailsDto;
import io.spaceurgent.gen.ai.thread.application.dto.ImageThreadDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/image-threads")
@RequiredArgsConstructor
public class ImageThreadController {

    private final ImageThreadService imageThreadService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ImageThreadDto> getImageThreads() {
        return imageThreadService.getAll();
    }

    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ImageThreadDetailsDto getImageThread(@PathVariable UUID id) {
        return imageThreadService.get(id);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ImageThreadDetailsDto createImageThread(@RequestBody @Valid ImageThreadRequest request) {
        return imageThreadService.create(request);
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ImageThreadDetailsDto updateImageThread(@PathVariable UUID id, @RequestBody @Valid GenImageRequest request) {
        return imageThreadService.update(id, request);
    }
}
