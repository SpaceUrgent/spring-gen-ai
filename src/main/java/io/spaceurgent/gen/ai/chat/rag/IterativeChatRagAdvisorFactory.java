package io.spaceurgent.gen.ai.chat.rag;

import io.spaceurgent.gen.ai.ai.ChatClientBuilders;
import io.spaceurgent.gen.ai.book.application.service.BookService;
import io.spaceurgent.gen.ai.chat.rag.generation.ExplainingQueryAugmenter;
import io.spaceurgent.gen.ai.chat.rag.preretrieval.HistoryCompressionQueryTransformer;
import io.spaceurgent.gen.ai.chat.rag.preretrieval.TranslatedQueryTransformer;
import io.spaceurgent.gen.ai.chat.rag.retrieval.BookSearchTools;
import io.spaceurgent.gen.ai.chat.rag.retrieval.IterativeBookRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.spaceurgent.gen.ai.shared.rag.BookMetadata.BOOK_TITLE;

@Component
@RequiredArgsConstructor
public class IterativeChatRagAdvisorFactory {

    private static final String REWRITE_QUERY_PROMPT = """
            Given a user query, rewrite it to provide better results when querying a {target}.
            Your task is ONLY to improve the wording of the query for retrieval.

            STRICT RULES:

            1. Preserve the exact meaning and intent of the original query.
            2. Do NOT infer what the user "probably meant".
            3. Do NOT correct ambiguous words.
            4. Do NOT replace nouns, entities, objects, animals, products, people,
               places, or fictional terms with something more common.
            5. Do NOT introduce information that is not explicitly present in the
               original query.
            6. Do NOT remove meaningful words, including book titles.
            7. You MAY:
               - improve grammar;
               - reorder words;
               - remove unnecessary conversational words;
               - make the question concise;
               - normalize wording without changing meaning.
            8. If the original query is already clear and concise, return it unchanged.
            9. If a word looks like a typo but its intended replacement is not
               completely unambiguous, KEEP THE ORIGINAL WORD.
            10. Return ONLY the rewritten query, in English. No explanation.

            Original query:
            {query}

            Rewritten query:
            """;

    private final BookSearchTools bookSearchTools;
    private final BookService bookService;
    private final ChatRagProperties properties;

    public RetrievalAugmentationAdvisor create(ChatClient.Builder chatClientBuilder) {
        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(
                        new TranslatedQueryTransformer(),
                        new HistoryCompressionQueryTransformer(ChatClientBuilders.withoutThinking(chatClientBuilder)),
                        RewriteQueryTransformer.builder()
                                .chatClientBuilder(ChatClientBuilders.withoutThinking(chatClientBuilder))
                                .promptTemplate(PromptTemplate.builder().template(REWRITE_QUERY_PROMPT).build())
                                .targetSearchSystem("vector store of book passages")
                                .build())
                .documentRetriever(new IterativeBookRetriever(
                        ChatClientBuilders.withoutThinking(chatClientBuilder),
                        bookSearchTools,
                        bookService,
                        properties))
                .queryAugmenter(new ExplainingQueryAugmenter(IterativeChatRagAdvisorFactory::formatDocuments))
                .build();
    }

    private static String formatDocuments(List<Document> documents) {
        return IntStream.range(0, documents.size())
                .mapToObj(i -> "[%d] (%s)\n%s".formatted(i + 1, documents.get(i).getMetadata().get(BOOK_TITLE),
                        documents.get(i).getText()))
                .collect(Collectors.joining("\n\n"));
    }
}
