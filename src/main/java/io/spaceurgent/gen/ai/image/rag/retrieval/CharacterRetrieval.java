package io.spaceurgent.gen.ai.image.rag.retrieval;

import io.spaceurgent.gen.ai.image.rag.preretrieval.CharacterQueryExpander;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import io.spaceurgent.gen.ai.image.application.dto.IllustrationQuery;

@Component
@RequiredArgsConstructor
public class CharacterRetrieval {

    private final CharacterQueryExpander characterQueryExpander;
    private final PassageSearch passageSearch;

    public List<Document> retrieve(IllustrationQuery query) {
        return passageSearch.search(characterQueryExpander.expand(query.toSearchQuery()));
    }
}
