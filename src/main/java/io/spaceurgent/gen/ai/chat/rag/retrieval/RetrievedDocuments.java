package io.spaceurgent.gen.ai.chat.rag.retrieval;

import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RetrievedDocuments {

    public static final String TOOL_CONTEXT_KEY = "retrievedDocuments";

    private final int maxSearches;
    private final int maxDocuments;
    private final Map<String, Document> documents = new LinkedHashMap<>();
    private int searches;

    public RetrievedDocuments(int maxSearches, int maxDocuments) {
        this.maxSearches = maxSearches;
        this.maxDocuments = maxDocuments;
    }

    public synchronized boolean tryStartSearch() {
        if (searches >= maxSearches) {
            return false;
        }
        searches++;
        return true;
    }

    public synchronized void addAll(List<Document> found) {
        for (Document document : found) {
            if (documents.size() >= maxDocuments) {
                return;
            }
            documents.putIfAbsent(document.getId(), document);
        }
    }

    public synchronized int searches() {
        return searches;
    }

    public synchronized List<Document> documents() {
        return new ArrayList<>(documents.values());
    }
}
