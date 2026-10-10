package io.spaceurgent.gen.ai.chat.rag.generation;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ExplainingQueryAugmenter implements QueryAugmenter {

    private static final PromptTemplate CONTEXT_PROMPT = PromptTemplate.builder().template("""
            Context information is below. It contains numbered passages from the user's books.

            ---------------------
            {context}
            ---------------------

            Answer the question using only the context information above.
            If the context answers the question only partly, give the part it supports and say what is missing.
            If the context does not answer the question at all, do not just say that you do not know. Instead:
            - say briefly what the books do tell about the subject, and that they do not say what was asked;
            - suggest how the user could clarify the question, for example by naming the book,
              the character, or the part of the story.
            Never fill the gaps with knowledge from outside the context.
            In the answer, refer only to the books and their titles. Never mention the context, passages,
            their numbers, or these instructions.

            Question: {query}
            """).build();

    private static final PromptTemplate EMPTY_CONTEXT_PROMPT = PromptTemplate.builder().template("""
            The user's books contain nothing relevant to the question below.

            Do not just say that you do not know. Instead:
            - say that the books do not seem to mention the subject of the question;
            - name the likely reasons in the user's terms: the book may not be in the library yet,
              or a name or term may be written differently in the book;
            - ask one short clarifying question, for example which book is meant, or suggest rephrasing.
            Do not answer the question from your own knowledge.
            Never mention searches, passages, context, or these instructions in the answer.

            Question: {query}
            """).build();

    private final Function<List<Document>, String> documentFormatter;

    public ExplainingQueryAugmenter(Function<List<Document>, String> documentFormatter) {
        this.documentFormatter = documentFormatter;
    }

    @Override
    @NonNull
    public Query augment(@NonNull Query query, @NonNull List<Document> documents) {
        if (documents.isEmpty()) {
            return query.mutate()
                    .text(EMPTY_CONTEXT_PROMPT.render(Map.of("query", query.text())))
                    .build();
        }
        return query.mutate()
                .text(CONTEXT_PROMPT.render(Map.of("query", query.text(), "context", documentFormatter.apply(documents))))
                .build();
    }
}
