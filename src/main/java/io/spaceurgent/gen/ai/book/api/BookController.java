package io.spaceurgent.gen.ai.book.api;

import io.spaceurgent.gen.ai.book.application.service.UploadBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final UploadBookService uploadBookService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void upload(@RequestParam("title") String title,
                       @RequestParam("file") MultipartFile file) {
        uploadBookService.upload(title, file);
    }
}
