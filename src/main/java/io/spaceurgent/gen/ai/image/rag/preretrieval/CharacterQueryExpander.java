package io.spaceurgent.gen.ai.image.rag.preretrieval;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

import static io.spaceurgent.gen.ai.image.rag.ImageQueryContext.CHARACTERS;

@Component
public class CharacterQueryExpander implements QueryExpander {

    @Override
    @NonNull
    public List<Query> expand(@NonNull Query query) {
        if (!(query.context().get(CHARACTERS) instanceof List<?> characters)) {
            return List.of();
        }
        return characters.stream()
                .map(String::valueOf)
                .filter(StringUtils::hasText)
                .map(name -> query.mutate()
                        .text("Physical appearance, face, clothing and belongings of " + name)
                        .build())
                .toList();
    }
}
