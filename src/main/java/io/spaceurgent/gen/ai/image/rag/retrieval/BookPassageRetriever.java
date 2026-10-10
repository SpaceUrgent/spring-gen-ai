package io.spaceurgent.gen.ai.image.rag.retrieval;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

import static io.spaceurgent.gen.ai.shared.rag.BookMetadata.BOOK_ID;
import io.spaceurgent.gen.ai.image.rag.ImageRagProperties;

@Component
@RequiredArgsConstructor
public class BookPassageRetriever implements DocumentRetriever {

    private final VectorStore vectorStore;
    private final ImageRagProperties properties;

    @Override
    @NonNull
    public List<Document> retrieve(@NonNull Query query) {
        Object bookId = query.context().get(BOOK_ID);
        Assert.notNull(bookId, "query context must contain " + BOOK_ID);
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query.text())
                .topK(properties.topK())
                .similarityThreshold(properties.similarityThreshold())
                .filterExpression("%s == '%s'".formatted(BOOK_ID, bookId))
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }
}
