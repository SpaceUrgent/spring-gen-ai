package io.spaceurgent.gen.ai.chat.rag.retrieval;

import io.spaceurgent.gen.ai.book.application.service.BookService;
import io.spaceurgent.gen.ai.chat.rag.ChatRagProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.spaceurgent.gen.ai.shared.rag.BookMetadata.BOOK_ID;
import static io.spaceurgent.gen.ai.shared.rag.BookMetadata.BOOK_TITLE;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSearchTools {

    static final int MIN_QUERY_WORDS = 3;

    private final BookService bookService;
    private final VectorStore vectorStore;
    private final ChatRagProperties properties;

    @Tool(name = "searchPassages", description = """
            Search the books for passages relevant to a query. \
            bookId is optional: pass it to search one book, or omit it to search all books.""")
    public String searchPassages(
            @ToolParam(description = """
                    Descriptive English search phrase of 5 to 15 words, never a single word. \
                    Use names, places and terms that appear in the book, for example \
                    "conditioning of children in the hatchery" instead of "children".""") String query,
            @ToolParam(required = false, description = """
                    Optional. Id of the book to search, exactly as given in the book list. \
                    Omit it to search all books.""") @Nullable String bookId,
            ToolContext toolContext) {
        if (!StringUtils.hasText(query)) {
            return "The query is empty. Call searchPassages with a descriptive search phrase.";
        }
        if (query.strip().split("\\s+").length < MIN_QUERY_WORDS) {
            log.debug("Tool searchPassages rejected a too short query '{}'", query);
            return "The query '%s' is too short. Use a descriptive phrase of 5 to 15 words.".formatted(query);
        }

        UUID book = StringUtils.hasText(bookId) ? parseBookId(bookId.strip()) : null;
        RetrievedDocuments retrieved = (RetrievedDocuments) toolContext.getContext().get(RetrievedDocuments.TOOL_CONTEXT_KEY);
        if (!retrieved.tryStartSearch()) {
            return "Search limit reached. Reply DONE.";
        }
        List<Document> documents = search(query, book);
        retrieved.addAll(documents);
        log.debug("Tool searchPassages(query='{}', bookId={}) found {} passages", query, book, documents.size());
        if (documents.isEmpty()) {
            return "No passages found. Try a different phrase, another book, or search without a bookId.";
        }
        return format(documents);
    }

    public List<Document> search(String query, @Nullable UUID bookId) {
        SearchRequest.Builder request = SearchRequest.builder()
                .query(query)
                .topK(properties.topK())
                .similarityThreshold(properties.similarityThreshold());
        if (bookId != null) {
            request.filterExpression(new FilterExpressionBuilder().eq(BOOK_ID, bookId.toString()).build());
        }
        return vectorStore.similaritySearch(request.build());
    }

    static String format(List<Document> documents) {
        return IntStream.range(0, documents.size())
                .mapToObj(i -> "[%d] (%s) %s".formatted(i + 1, documents.get(i).getMetadata().get(BOOK_TITLE),
                        documents.get(i).getText()))
                .collect(Collectors.joining("\n\n"));
    }

    private @Nullable UUID parseBookId(String bookId) {
        try {
            UUID id = UUID.fromString(bookId);
            return bookService.existsById(id) ? id : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
