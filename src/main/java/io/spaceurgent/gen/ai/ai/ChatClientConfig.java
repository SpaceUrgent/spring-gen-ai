package io.spaceurgent.gen.ai.ai;

import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Value("${chat-client.enable-logging:false}")
    private boolean enableChatClientLogging;

    @Bean
    ChatClientBuilderCustomizer loggingChatClientCustomizer() {
        return builder -> {
            if (enableChatClientLogging) {
                builder.defaultAdvisors(new SimpleLoggerAdvisor());
            }
        };
    }
}
