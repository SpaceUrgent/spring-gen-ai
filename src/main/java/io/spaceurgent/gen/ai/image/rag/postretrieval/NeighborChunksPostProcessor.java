package io.spaceurgent.gen.ai.image.rag.postretrieval;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static io.spaceurgent.gen.ai.shared.rag.BookMetadata.BOOK_ID;
import static io.spaceurgent.gen.ai.shared.rag.BookMetadata.CHUNK_INDEX;
import io.spaceurgent.gen.ai.image.rag.ImageRagProperties;

@Component
@RequiredArgsConstructor
public class NeighborChunksPostProcessor implements DocumentPostProcessor {

    private final BookChunkRepository bookChunkRepository;
    private final ImageRagProperties properties;

    @Override
    @NonNull
    public List<Document> process(@NonNull Query query, @NonNull List<Document> documents) {
        Object bookId = query.context().get(BOOK_ID);
        if (bookId == null || documents.isEmpty()) {
            return documents;
        }

        TreeSet<Integer> chunkIndexes = new TreeSet<>();
        List<Document> withoutIndex = new ArrayList<>();
        for (Document document : documents) {
            if (chunkIndexes.size() + withoutIndex.size() >= properties.maxChunks()) {
                break;
            }
            if (!(document.getMetadata().get(CHUNK_INDEX) instanceof Number index)) {
                withoutIndex.add(document);
                continue;
            }
            int hit = index.intValue();
            chunkIndexes.add(hit);
            for (int distance = 1; distance <= properties.neighborChunks(); distance++) {
                for (int neighbor : new int[]{hit - distance, hit + distance}) {
                    if (neighbor >= 0 && chunkIndexes.size() + withoutIndex.size() < properties.maxChunks()) {
                        chunkIndexes.add(neighbor);
                    }
                }
            }
        }

        List<Document> passages = mergeAdjacent(String.valueOf(bookId),
                bookChunkRepository.findByChunkIndexes(String.valueOf(bookId), chunkIndexes));
        passages.addAll(withoutIndex);
        return passages;
    }

    private static List<Document> mergeAdjacent(String bookId, List<Document> chunks) {
        TreeMap<Integer, String> textByIndex = new TreeMap<>();
        chunks.forEach(chunk -> textByIndex.put(((Number) chunk.getMetadata().get(CHUNK_INDEX)).intValue(), chunk.getText()));

        List<List<Integer>> runs = new ArrayList<>();
        textByIndex.keySet().forEach(index -> {
            if (runs.isEmpty() || runs.getLast().getLast() != index - 1) {
                runs.add(new ArrayList<>());
            }
            runs.getLast().add(index);
        });

        List<Document> passages = new ArrayList<>();
        for (List<Integer> run : runs) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put(BOOK_ID, bookId);
            metadata.put(CHUNK_INDEX, run.getFirst());
            passages.add(Document.builder()
                    .id("%s:%d-%d".formatted(bookId, run.getFirst(), run.getLast()))
                    .text(run.stream().map(textByIndex::get).collect(Collectors.joining("\n")))
                    .metadata(metadata)
                    .build());
        }
        return passages;
    }
}
