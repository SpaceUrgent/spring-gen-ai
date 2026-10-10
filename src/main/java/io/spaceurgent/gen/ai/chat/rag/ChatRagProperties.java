package io.spaceurgent.gen.ai.chat.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.chat.rag")
public record ChatRagProperties(
        @DefaultValue("5") int topK,
        @DefaultValue("0.45") double similarityThreshold,
        @DefaultValue("3") int maxSearches,
        @DefaultValue("4") int maxToolRounds,
        @DefaultValue("15") int maxDocuments,
        @DefaultValue("20") int memoryMaxMessages,
        @DefaultValue("2") int maxRepeatAttempts
) {
}
