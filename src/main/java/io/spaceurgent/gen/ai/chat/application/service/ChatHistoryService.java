package io.spaceurgent.gen.ai.chat.application.service;

import io.spaceurgent.gen.ai.chat.domain.model.Chat;
import io.spaceurgent.gen.ai.chat.domain.model.ChatMessage;
import io.spaceurgent.gen.ai.chat.application.dto.ChatMessageDto;
import io.spaceurgent.gen.ai.chat.domain.repository.ChatMessageRepository;
import io.spaceurgent.gen.ai.chat.domain.repository.ChatRepository;
import io.spaceurgent.gen.ai.chat.application.dto.ChatDetailsDto;
import io.spaceurgent.gen.ai.chat.application.dto.ChatDto;
import io.spaceurgent.gen.ai.chat.rag.ChatRagProperties;
import io.spaceurgent.gen.ai.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private static final int TITLE_LENGTH = 50;

    private final ChatRepository chatRepository;
    private final ChatMessageRepository messageRepository;
    private final PersistentMemoryRagChatService persistentMemoryRagChatService;
    private final ChatRagProperties ragProperties;

    public List<ChatDto> getAll() {
        return chatRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(chat -> new ChatDto(chat.getId(), chat.getTitle()))
                .toList();
    }

    public ChatDetailsDto get(UUID id) {
        Chat chat = getChatById(id);
        List<ChatMessageDto> messages = messageRepository.findAllByChatIdOrderByCreatedAt(id).stream()
                .map(ChatHistoryService::toChatMessageDto)
                .toList();
        return new ChatDetailsDto(chat.getId(), chat.getTitle(), messages);
    }

    public ChatDto startWithPersistentMemory(String question) {
        Chat chat = chatRepository.save(Chat.builder()
                .id(UUID.randomUUID())
                .title(extractTitle(question))
                .build());
        persistentMemoryRagChatService.answer(chat.getId(), question);
        return new ChatDto(chat.getId(), chat.getTitle());
    }

    public List<ChatMessageDto> sendWithPersistentMemory(UUID chatId, String question) {
        getChatById(chatId);
        int storedCount = messageRepository.findAllByChatIdOrderByCreatedAt(chatId).size();
        try {
            persistentMemoryRagChatService.answer(chatId, question);
        } catch (RuntimeException e) {
            List<ChatMessage> messages = messageRepository.findAllByChatIdOrderByCreatedAt(chatId);
            messageRepository.deleteAll(messages.subList(Math.min(storedCount, messages.size()), messages.size()));
            throw e;
        }
        List<ChatMessage> messages = messageRepository.findAllByChatIdOrderByCreatedAt(chatId);
        return messages.subList(storedCount, messages.size()).stream()
                .map(ChatHistoryService::toChatMessageDto)
                .toList();
    }

    private Chat getChatById(UUID id) {
        return chatRepository.findById(id).orElseThrow(() -> new BadRequestException("Chat with id %s not found".formatted(id)));
    }

    private static String extractTitle(String question) {
        String title = question.strip().replaceAll("\\s+", " ");
        return title.length() <= TITLE_LENGTH ? title : title.substring(0, TITLE_LENGTH).strip() + "…";
    }

    private static ChatMessageDto toChatMessageDto(ChatMessage message) {
        return new ChatMessageDto(message.getRole(), message.getContent(), message.getCreatedAt());
    }
}
