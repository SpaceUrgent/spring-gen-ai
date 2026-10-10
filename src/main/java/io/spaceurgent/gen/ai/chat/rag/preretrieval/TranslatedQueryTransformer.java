package io.spaceurgent.gen.ai.chat.rag.preretrieval;

import io.spaceurgent.gen.ai.chat.rag.ChatRagContext;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;

public class TranslatedQueryTransformer implements QueryTransformer {

    @Override
    @NonNull
    public Query transform(@NonNull Query query) {
        if (query.context().get(ChatRagContext.ENGLISH_QUERY) instanceof String englishQuery && !englishQuery.isBlank()) {
            return query.mutate().text(englishQuery).build();
        }
        return query;
    }
}
