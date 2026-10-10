package io.spaceurgent.gen.ai.image.rag.retrieval;

import io.spaceurgent.gen.ai.image.rag.ImageRagProperties;
import io.spaceurgent.gen.ai.image.rag.postretrieval.NeighborChunksPostProcessor;
import io.spaceurgent.gen.ai.image.rag.preretrieval.SceneQueryExpander;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.spaceurgent.gen.ai.image.application.exception.IllustrationNotPossibleException.Reason.SCENE_NOT_FOUND;
import io.spaceurgent.gen.ai.image.application.dto.IllustrationQuery;
import io.spaceurgent.gen.ai.image.rag.CharacterNames;
import io.spaceurgent.gen.ai.image.application.exception.IllustrationNotPossibleException;

@Component
@RequiredArgsConstructor
public class SceneRetrieval {

    public record Result(IllustrationQuery query, List<Document> passages) {
    }

    private final SceneQueryExpander sceneQueryExpander;
    private final PassageSearch passageSearch;
    private final NeighborChunksPostProcessor neighborChunks;
    private final SceneCharacterFinder characterFinder;
    private final ImageRagProperties properties;

    public Result retrieve(IllustrationQuery query) {
        if (!query.sceneRequested()) {
            return new Result(query, List.of());
        }
        Query searchQuery = query.toSearchQuery();
        List<Document> passages = passageSearch.search(sceneQueryExpander.expand(searchQuery));
        if (passages.isEmpty()) {
            throw new IllustrationNotPossibleException(SCENE_NOT_FOUND);
        }
        List<String> present = characterFinder.find(query, neighborChunks.process(searchQuery, passages));
        List<String> characters = CharacterNames.merge(query.characters(), present, properties.maxCharacters());
        return new Result(query.withCharacters(characters), passages);
    }
}
