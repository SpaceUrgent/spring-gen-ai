package io.spaceurgent.gen.ai.chat.ui;

import io.spaceurgent.gen.ai.chat.application.dto.ChatDetailsDto;
import io.spaceurgent.gen.ai.chat.application.dto.ChatDto;
import io.spaceurgent.gen.ai.chat.application.service.ChatHistoryService;
import io.spaceurgent.gen.ai.ui.Htmx;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatUiController {

    private final ChatHistoryService chatHistoryService;

    @GetMapping("/new")
    public String newChat(Model model) {
        model.addAttribute("chat", null);
        model.addAttribute("messages", List.of());
        return "chats/chat";
    }

    @PostMapping
    public void start(@RequestParam String message, HttpServletResponse response) {
        ChatDto chat = chatHistoryService.startWithPersistentMemory(requireText(message));
        Htmx.redirect(response, "/chats/" + chat.id());
    }

    @GetMapping("/{id}")
    public String chat(@PathVariable UUID id, Model model) {
        ChatDetailsDto chat = chatHistoryService.get(id);
        model.addAttribute("chat", chat);
        model.addAttribute("messages", chat.messages());
        return "chats/chat";
    }

    @PostMapping("/{id}/messages")
    public String send(@PathVariable UUID id, @RequestParam String message, Model model, HttpServletResponse response) {
        model.addAttribute("messages", chatHistoryService.sendWithPersistentMemory(id, requireText(message)));
        Htmx.trigger(response, "message-sent");
        return "chats/chat :: messages";
    }

    private static String requireText(String message) {
        if (!StringUtils.hasText(message)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type a question first");
        }
        return message.strip();
    }
}
