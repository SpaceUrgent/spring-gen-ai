package io.spaceurgent.gen.ai.chat.rag.preretrieval;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;

public class HistoryCompressionQueryTransformer implements QueryTransformer {

    private static final String PROMPT = """
            Given the conversation history and a follow-up question, rewrite the follow-up question
            as a standalone question in English. Include every detail from the history that is needed
            to understand it: book title, character names, places and events.

            Rules:
            - If the follow-up question is already standalone, return it unchanged.
            - Do not answer the question.
            - Do not add anything that is not in the history or the question.
            - Return ONLY the standalone question.

            Conversation history:
            {history}

            Follow-up question:
            {query}

            Standalone question:
            """;

    private final CompressionQueryTransformer delegate;

    public HistoryCompressionQueryTransformer(ChatClient.Builder chatClientBuilder) {
        this.delegate = CompressionQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .promptTemplate(PromptTemplate.builder().template(PROMPT).build())
                .build();
    }

    @Override
    @NonNull
    public Query transform(@NonNull Query query) {
        long conversationMessages = query.history().stream()
                .filter(message -> message.getMessageType() == MessageType.USER
                        || message.getMessageType() == MessageType.ASSISTANT)
                .count();
        return conversationMessages > 1 ? delegate.transform(query) : query;
    }
}
