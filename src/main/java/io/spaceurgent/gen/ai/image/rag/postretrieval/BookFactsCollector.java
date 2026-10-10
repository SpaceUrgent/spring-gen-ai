package io.spaceurgent.gen.ai.image.rag.postretrieval;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import io.spaceurgent.gen.ai.image.application.dto.BookFacts;
import io.spaceurgent.gen.ai.image.application.dto.IllustrationQuery;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookFactsCollector {

    private final DocumentJoiner documentJoiner;
    private final NeighborChunksPostProcessor neighborChunks;
    private final VisualFactsCondenser condenser;

    public BookFacts collect(IllustrationQuery query, List<Document> scenePassages, List<Document> characterPassages) {
        Map<Query, List<List<Document>>> rounds = new LinkedHashMap<>();
        rounds.put(new Query("scene"), List.of(scenePassages));
        rounds.put(new Query("characters"), List.of(characterPassages));
        List<Document> passages = neighborChunks.process(query.toSearchQuery(), documentJoiner.join(rounds));
        log.debug("Characters: {}, passages: {} scene hits, {} character hits, {} after restoring surrounding text",
                query.characters(), scenePassages.size(), characterPassages.size(), passages.size());
        return condenser.condense(query, passages);
    }
}
