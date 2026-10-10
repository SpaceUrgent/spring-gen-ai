package io.spaceurgent.gen.ai.image.rag;

import org.springframework.ai.document.Document;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Passages {
    private Passages() {
    }

    public static String numbered(List<Document> documents) {
        return IntStream.range(0, documents.size())
                .mapToObj(i -> "[%d] %s".formatted(i + 1, documents.get(i).getText()))
                .collect(Collectors.joining("\n\n"));
    }

    public static String plain(List<Document> documents) {
        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }
}
