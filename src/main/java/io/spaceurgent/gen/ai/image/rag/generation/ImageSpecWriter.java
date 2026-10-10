package io.spaceurgent.gen.ai.image.rag.generation;

import io.spaceurgent.gen.ai.image.rag.ImageSpecConfig;
import io.spaceurgent.gen.ai.image.application.dto.ImageSpec;
import io.spaceurgent.gen.ai.image.application.dto.ImageSpecDraft;
import io.spaceurgent.gen.ai.image.rag.preretrieval.StyleDetailsText;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import io.spaceurgent.gen.ai.image.application.dto.ImageRequirements;
import io.spaceurgent.gen.ai.image.application.dto.BookFacts;
import io.spaceurgent.gen.ai.image.application.dto.IllustrationQuery;
import io.spaceurgent.gen.ai.image.rag.ImageSpecProperties;
import io.spaceurgent.gen.ai.image.application.exception.SpecGenerationException;

@Component
public class ImageSpecWriter {

    private static final String NOT_SPECIFIED = "not specified";

    private static final String PROMPT = """
            You are an art director preparing a brief for an image generation model.
            Create an image specification that is as detailed and visually concrete as possible
            and depicts exactly what the user asked for.

            Rules:
            1. Depict the moment and the characters the request asks for. Do not switch to another scene.
            2. Book facts are the source of truth for characters, setting and objects. Never contradict them.
            3. When the book facts do not specify a visual detail, choose a plausible one consistent with the
               book's era, setting and tone, and add a short note about it to "assumptions".
            4. Required style is defined by the user and is final. Use its values word for word in "prompt",
               do not rephrase, translate, extend or replace them. Ignore style words in the request that contradict it.
               If no style is required, use only style words stated in the request and do not invent a style.
            5. Things to avoid are defined by the user. Do not depict them and do not mention them in "prompt".
            6. Choose composition and framing that suit the required orientation.
            7. Describe only what is visible: shapes, colors, materials, light. No backstory, no inner thoughts.
            8. Include every character that appears in the requested moment.
            9. "prompt" must be self-contained: a reader of only "prompt" should be able to draw the image.
            """;

    private static final String USER = """
            Request to illustrate:
            %s

            Required style:
            %s

            Things to avoid:
            %s

            Orientation:
            %s

            Book facts:
            %s
            """;

    private final ChatClient chatClient;
    private final StructuredOutputValidationAdvisor validationAdvisor;

    ImageSpecWriter(@Qualifier(ImageSpecConfig.CHAT_CLIENT) ChatClient chatClient, ImageSpecProperties properties) {
        this.chatClient = chatClient;
        this.validationAdvisor = StructuredOutputValidationAdvisor.builder()
                .outputType(ImageSpecDraft.class)
                .maxRepeatAttempts(properties.maxRepeatAttempts())
                .build();
    }

    public ImageSpec write(IllustrationQuery query, BookFacts facts, ImageRequirements requirements) {
        ImageSpecDraft draft = chatClient.prompt()
                .advisors(validationAdvisor)
                .system(PROMPT)
                .user(userMessage(query, facts, requirements))
                .call()
                .entity(ImageSpecDraft.class);
        if (draft == null) {
            throw new SpecGenerationException("Model returned no illustration spec");
        }
        return new ImageSpec(
                draft.title(),
                requirements.orientation().value(),
                draft.scene(),
                draft.characters(),
                draft.environment(),
                draft.composition(),
                requirements.style(),
                draft.prompt(),
                requirements.negativePrompt(),
                draft.assumptions());
    }

    private static String userMessage(IllustrationQuery query, BookFacts facts, ImageRequirements requirements) {
        String style = String.join(", ", StyleDetailsText.format(requirements.style()).lines().toList());
        return USER.formatted(
                query.request(),
                style.isEmpty() ? NOT_SPECIFIED : style,
                requirements.negativePrompt() == null ? NOT_SPECIFIED : requirements.negativePrompt(),
                requirements.orientation().value(),
                facts.text());
    }
}
