package io.spaceurgent.gen.ai.image.rag;

import io.spaceurgent.gen.ai.ai.ChatClientBuilders;
import io.spaceurgent.gen.ai.image.rag.retrieval.RoundRobinDocumentJoiner;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ImageSpecProperties.class)
public class ImageSpecConfig {

    public static final String FAST_CHAT_CLIENT = "imageSpecFastChatClient";
    public static final String CHAT_CLIENT = "imageSpecChatClient";

    @Bean(FAST_CHAT_CLIENT)
    ChatClient imageSpecFastChatClient(ChatClient.Builder chatClientBuilder) {
        return ChatClientBuilders.withoutThinking(chatClientBuilder).build();
    }

    @Bean(CHAT_CLIENT)
    ChatClient imageSpecChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.clone().build();
    }

    @Bean
    DocumentJoiner imageSpecDocumentJoiner() {
        return new RoundRobinDocumentJoiner();
    }
}
