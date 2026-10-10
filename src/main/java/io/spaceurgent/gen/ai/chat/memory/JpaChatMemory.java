package io.spaceurgent.gen.ai.chat.memory;

import io.spaceurgent.gen.ai.chat.domain.model.Chat;
import io.spaceurgent.gen.ai.chat.domain.model.ChatMessage;
import io.spaceurgent.gen.ai.chat.domain.model.ChatMessageRole;
import io.spaceurgent.gen.ai.chat.domain.repository.ChatMessageRepository;
import io.spaceurgent.gen.ai.chat.domain.repository.ChatRepository;
import io.spaceurgent.gen.ai.chat.rag.ChatRagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaChatMemory implements ChatMemory {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatRagProperties properties;

    @Override
    public void add(String conversationId, List<Message> messages) {
        Chat chat = chatRepository.getReferenceById(UUID.fromString(conversationId));
        messages.stream()
                .filter(message -> message.getMessageType() == MessageType.USER
                        || message.getMessageType() == MessageType.ASSISTANT)
                .map(message -> ChatMessage.builder()
                        .id(UUID.randomUUID())
                        .chat(chat)
                        .role(message.getMessageType() == MessageType.USER ? ChatMessageRole.USER : ChatMessageRole.ASSISTANT)
                        .content(message.getText() == null ? "" : message.getText())
                        .build())
                .forEach(messageRepository::save);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<ChatMessage> messages = messageRepository.findAllByChatIdOrderByCreatedAt(UUID.fromString(conversationId));
        return messages.subList(Math.max(0, messages.size() - properties.memoryMaxMessages()), messages.size()).stream()
                .<Message>map(message -> message.getRole() == ChatMessageRole.USER
                        ? new UserMessage(message.getContent())
                        : new AssistantMessage(message.getContent()))
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        messageRepository.deleteAll(messageRepository.findAllByChatIdOrderByCreatedAt(UUID.fromString(conversationId)));
    }
}
