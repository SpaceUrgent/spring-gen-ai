package io.spaceurgent.gen.ai.image.rag.preretrieval;

import io.spaceurgent.gen.ai.ai.ChatClientBuilders;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

import static io.spaceurgent.gen.ai.image.rag.ImageQueryContext.SCENE_QUERY;

@Component
public class SceneQueryExpander implements QueryExpander {

    private static final int SCENE_QUERY_VARIANTS = 3;

    private final MultiQueryExpander delegate;

    public SceneQueryExpander(ChatClient.Builder chatClientBuilder) {
        this.delegate = MultiQueryExpander.builder()
                .chatClientBuilder(ChatClientBuilders.withoutThinking(chatClientBuilder))
                .numberOfQueries(SCENE_QUERY_VARIANTS)
                .includeOriginal(true)
                .build();
    }

    @Override
    @NonNull
    public List<Query> expand(@NonNull Query query) {
        if (!(query.context().get(SCENE_QUERY) instanceof String sceneQuery) || !StringUtils.hasText(sceneQuery)) {
            return List.of();
        }
        return delegate.expand(query.mutate().text(sceneQuery).build()).stream()
                .map(variant -> query.mutate().text(variant.text()).build())
                .toList();
    }
}
