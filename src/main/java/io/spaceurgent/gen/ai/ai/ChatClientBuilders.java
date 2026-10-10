package io.spaceurgent.gen.ai.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;

public final class ChatClientBuilders {
    private ChatClientBuilders() {
    }

    public static ChatClient.Builder withoutThinking(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.clone()
                .defaultOptions(OllamaChatOptions.builder().disableThinking());
    }
}
