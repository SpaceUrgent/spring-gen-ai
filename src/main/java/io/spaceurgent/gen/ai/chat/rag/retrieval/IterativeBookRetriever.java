package io.spaceurgent.gen.ai.chat.rag.retrieval;

import io.spaceurgent.gen.ai.book.application.service.BookService;
import io.spaceurgent.gen.ai.chat.rag.ChatRagProperties;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class IterativeBookRetriever implements DocumentRetriever {

    private static final String PROMPT = """
            You find passages in the user's book library that answer a question.
            You do not answer the question yourself.

            You get the list of books, the question, and the passages found by searching the full question.
            Your job is to find what these passages miss.

            Steps:
            1. Decide which book the question is about. If the question names or clearly implies a book,
               pass its id to every search. Otherwise search without a bookId.
            2. Call searchPassages at least 2 and at most %d times. Every search must look at the question
               from a different angle. For a broad question, cover its different aspects, for example
               for a society: social classes, rules and government, family, beliefs, everyday life.
            3. Write each search query as a descriptive phrase of 5 to 15 words. Never search a single word.
               Prefer names, places and terms that appear in the passages, for example a character,
               an institution or an invented word, over general words.
            4. Do not repeat a search that you or the first search already made.
            5. When you have made your searches, or nothing more can be found, reply with the single word DONE.
            """;

    private static final String USER = """
            Books (id | title):
            %s

            Question: %s

            Passages found by searching the full question:
            %s
            """;

    private final ChatClient chatClient;
    private final BookSearchTools tools;
    private final BookService bookService;
    private final ChatRagProperties properties;

    public IterativeBookRetriever(ChatClient.Builder chatClientBuilder,
                                  BookSearchTools tools,
                                  BookService bookService,
                                  ChatRagProperties properties) {
        this.chatClient = chatClientBuilder
                .defaultTools(tools)
                .defaultAdvisors(ToolCallingAdvisor.builder()
                        .toolCallingManager(ToolCallingManager.builder()
                                .maxTotalToolCalls(properties.maxToolRounds())
                                .build())
                        .build())
                .build();
        this.tools = tools;
        this.bookService = bookService;
        this.properties = properties;
    }

    @Override
    @NonNull
    public List<Document> retrieve(@NonNull Query query) {
        String books = bookService.getAll().stream()
                .map(book -> "%s | %s".formatted(book.getId(), book.getTitle()))
                .collect(Collectors.joining("\n"));
        if (books.isEmpty()) {
            log.debug("The library is empty, nothing to retrieve");
            return List.of();
        }

        RetrievedDocuments retrieved = new RetrievedDocuments(properties.maxSearches(), properties.maxDocuments());
        List<Document> firstPassages = tools.search(query.text(), null);
        retrieved.addAll(firstPassages);
        log.debug("Search with the full question found {} passages", firstPassages.size());

        chatClient.prompt()
                .system(PROMPT.formatted(properties.maxSearches()))
                .user(USER.formatted(books, query.text(),
                        firstPassages.isEmpty() ? "None." : BookSearchTools.format(firstPassages)))
                .toolContext(Map.of(RetrievedDocuments.TOOL_CONTEXT_KEY, retrieved))
                .call()
                .content();
        log.debug("Retrieval agent finished after {} follow-up searches, {} passages in total",
                retrieved.searches(), retrieved.documents().size());
        return retrieved.documents();
    }
}
