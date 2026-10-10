package io.spaceurgent.gen.ai.book.application.service;

import io.spaceurgent.gen.ai.shared.rag.BookMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadBookService {
    private final BookService bookService;
    private final VectorStore vectorStore;

    @Transactional
    public void upload(String title, MultipartFile file) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        if (!MediaType.APPLICATION_PDF_VALUE.equals(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Only PDF files are supported");
        }
        try {
            UUID bookId = bookService.create(title);
            List<Document> documents = new CustomPdfReader(file.getResource()).read();
            TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                    .withChunkSize(100)
                    .build();
            List<Document> chunks = textSplitter.split(documents);
            for (int i = 0; i < chunks.size(); i++) {
                Map<String, Object> metadata = chunks.get(i).getMetadata();
                metadata.put(BookMetadata.BOOK_ID, bookId);
                metadata.put(BookMetadata.BOOK_TITLE, title);
                metadata.put(BookMetadata.CHUNK_INDEX, i);
            }
            vectorStore.add(chunks);
        } catch (Exception e) {
            log.error("Error upload book: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
