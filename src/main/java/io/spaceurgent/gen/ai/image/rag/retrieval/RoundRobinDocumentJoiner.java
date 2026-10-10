package io.spaceurgent.gen.ai.image.rag.retrieval;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RoundRobinDocumentJoiner implements DocumentJoiner {

    @Override
    @NonNull
    public List<Document> join(@NonNull Map<Query, List<List<Document>>> documentsForQuery) {
        List<Iterator<Document>> rankedLists = documentsForQuery.values().stream()
                .map(lists -> lists.stream().flatMap(List::stream).iterator())
                .toList();
        Map<String, Document> joined = new LinkedHashMap<>();
        List<Iterator<Document>> remaining = new ArrayList<>(rankedLists);
        while (!remaining.isEmpty()) {
            remaining.removeIf(documents -> !documents.hasNext());
            remaining.forEach(documents -> {
                Document document = documents.next();
                joined.putIfAbsent(document.getId(), document);
            });
        }
        return new ArrayList<>(joined.values());
    }
}
