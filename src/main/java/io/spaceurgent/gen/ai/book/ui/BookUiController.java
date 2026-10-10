package io.spaceurgent.gen.ai.book.ui;

import io.spaceurgent.gen.ai.book.application.service.BookService;
import io.spaceurgent.gen.ai.book.application.service.UploadBookService;
import io.spaceurgent.gen.ai.book.domain.Book;
import io.spaceurgent.gen.ai.ui.Htmx;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookUiController {

    private final BookService bookService;
    private final UploadBookService uploadBookService;

    @GetMapping
    public String books(Model model) {
        addBooks(model);
        return "books/index";
    }

    @PostMapping
    public String upload(@RequestParam String title, @RequestParam MultipartFile file, Model model, HttpServletResponse response) {
        uploadBookService.upload(title, file);
        addBooks(model);
        Htmx.trigger(response, "book-uploaded");
        return "books/index :: list";
    }

    private void addBooks(Model model) {
        model.addAttribute("books", bookService.getAll().stream()
                .sorted(Comparator.comparing(Book::getCreatedAt).reversed())
                .toList());
    }
}
