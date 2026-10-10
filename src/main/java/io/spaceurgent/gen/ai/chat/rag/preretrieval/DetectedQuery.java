package io.spaceurgent.gen.ai.chat.rag.preretrieval;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DetectedQuery(
        @JsonPropertyDescription("Name of the language the question is written in, in English, e.g. English, Ukrainian, German")
        String language,
        @JsonPropertyDescription("The question translated to English, or unchanged if it is already in English")
        String englishText
) {
}
