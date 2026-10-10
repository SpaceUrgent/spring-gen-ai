package io.spaceurgent.gen.ai.book.application.service;

import io.spaceurgent.gen.ai.book.domain.Book;
import io.spaceurgent.gen.ai.book.domain.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public UUID create(String title) {
        Objects.requireNonNull(title, "Title must not be null");
        Book book = Book.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .title(title)
                .build();
        return bookRepository.save(book).getId();
    }

    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    public boolean existsById(UUID id) {
        return bookRepository.existsById(id);
    }

    public Optional<Book> findById(UUID id) {
        return bookRepository.findById(id);
    }
}
