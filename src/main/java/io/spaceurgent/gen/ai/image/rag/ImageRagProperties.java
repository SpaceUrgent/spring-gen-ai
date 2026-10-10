package io.spaceurgent.gen.ai.image.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.image.rag")
public record ImageRagProperties(
        @DefaultValue("5") int topK,
        @DefaultValue("0.45") double similarityThreshold,
        @DefaultValue("1") int neighborChunks,
        @DefaultValue("24") int maxChunks,
        @DefaultValue("4") int maxCharacters
) {
}
