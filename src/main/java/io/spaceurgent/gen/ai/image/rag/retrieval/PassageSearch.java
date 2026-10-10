package io.spaceurgent.gen.ai.image.rag.retrieval;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PassageSearch {

    private final BookPassageRetriever retriever;
    private final DocumentJoiner documentJoiner;

    public List<Document> search(List<Query> queries) {
        Map<Query, List<List<Document>>> documentsForQuery = new LinkedHashMap<>();
        queries.forEach(query -> documentsForQuery.put(query, List.of(retriever.retrieve(query))));
        return documentJoiner.join(documentsForQuery);
    }
}
