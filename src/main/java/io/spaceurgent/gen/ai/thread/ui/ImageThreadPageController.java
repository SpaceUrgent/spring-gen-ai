package io.spaceurgent.gen.ai.thread.ui;

import io.spaceurgent.gen.ai.book.domain.Book;
import io.spaceurgent.gen.ai.book.application.service.BookService;
import io.spaceurgent.gen.ai.image.application.dto.Orientation;
import io.spaceurgent.gen.ai.thread.application.dto.GenImageMetadataDto;
import io.spaceurgent.gen.ai.thread.application.dto.ImageThreadDetailsDto;
import io.spaceurgent.gen.ai.thread.application.service.ImageThreadService;
import io.spaceurgent.gen.ai.ui.Htmx;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/threads")
@RequiredArgsConstructor
public class ImageThreadPageController {

    private final ImageThreadService imageThreadService;
    private final BookService bookService;
    private final ObjectMapper objectMapper;

    @InitBinder
    void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/new")
    public String newThread(@RequestParam(required = false) UUID bookId, Model model) {
        List<Book> books = bookService.getAll().stream()
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();
        ImageThreadForm form = new ImageThreadForm();
        form.setBookId(bookId);
        model.addAttribute("books", books);
        model.addAttribute("form", form);
        model.addAttribute("orientations", Orientation.values());
        return "threads/new";
    }

    @PostMapping
    public void create(@Valid @ModelAttribute ImageThreadForm form, BindingResult bindingResult, HttpServletResponse response) {
        if (form.getBookId() == null) {
            bindingResult.rejectValue("bookId", "required", "Choose a book");
        }
        Htmx.requireValid(bindingResult);
        ImageThreadDetailsDto thread = imageThreadService.create(form.toImageThreadRequest());
        Htmx.redirect(response, "/threads/" + thread.id());
    }

    @GetMapping("/{id}")
    public String thread(@PathVariable UUID id, Model model) {
        addThread(model, imageThreadService.get(id));
        return "threads/thread";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable UUID id, @Valid @ModelAttribute ImageThreadForm form, BindingResult bindingResult,
                         Model model, HttpServletResponse response) {
        Htmx.requireValid(bindingResult);
        addThread(model, imageThreadService.update(id, form.toGenImageRequest()));
        Htmx.trigger(response, "threads-changed");
        return "threads/thread :: body";
    }

    private void addThread(Model model, ImageThreadDetailsDto thread) {
        List<GenImageMetadataDto> generations = thread.generationResults().reversed();
        Map<UUID, String> specs = new LinkedHashMap<>();
        generations.forEach(generation -> specs.put(generation.getImageId(),
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(generation.getImageSpec())));
        model.addAttribute("thread", thread);
        model.addAttribute("generations", generations);
        model.addAttribute("specs", specs);
        model.addAttribute("form", ImageThreadForm.of(thread.userRequest()));
        model.addAttribute("orientations", Orientation.values());
    }
}
