package io.spaceurgent.gen.ai.image.application.dto;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.rag.Query;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.spaceurgent.gen.ai.shared.rag.BookMetadata.BOOK_ID;
import static io.spaceurgent.gen.ai.image.rag.ImageQueryContext.CHARACTERS;
import static io.spaceurgent.gen.ai.image.rag.ImageQueryContext.SCENE_QUERY;

// #rename to ImageSpecQuery

public record IllustrationQuery(UUID bookId, String request, @Nullable String sceneQuery, List<String> characters) {

    public IllustrationQuery {
        characters = List.copyOf(characters);
    }

    public boolean sceneRequested() {
        return StringUtils.hasText(sceneQuery);
    }

    public boolean hasSubject() {
        return sceneRequested() || !characters.isEmpty();
    }

    public IllustrationQuery withCharacters(List<String> characters) {
        return new IllustrationQuery(bookId, request, sceneQuery, characters);
    }

    public Query toSearchQuery() {
        Map<String, Object> context = new HashMap<>();
        context.put(BOOK_ID, bookId.toString());
        context.put(CHARACTERS, characters);
        if (sceneRequested()) {
            context.put(SCENE_QUERY, sceneQuery);
        }
        return Query.builder()
                .text(searchText())
                .context(context)
                .build();
    }

    private String searchText() {
        if (sceneRequested()) {
            return sceneQuery;
        }
        return characters.isEmpty() ? request : String.join(", ", characters);
    }
}
