package io.spaceurgent.gen.ai.ui;

import io.spaceurgent.gen.ai.chat.application.service.ChatHistoryService;
import io.spaceurgent.gen.ai.thread.application.service.ImageThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@RequiredArgsConstructor
public class LayoutController {

    private final ChatHistoryService chatHistoryService;
    private final ImageThreadService imageThreadService;

    @GetMapping("/")
    public String home() {
        return "redirect:/books";
    }

    @GetMapping("/nav/chats")
    public String chats(@RequestHeader(name = Htmx.CURRENT_URL, required = false) String currentUrl, Model model) {
        model.addAttribute("chats", chatHistoryService.getAll());
        model.addAttribute("currentUrl", currentUrl == null ? "" : currentUrl);
        return "fragments/nav :: chats";
    }

    @GetMapping("/nav/threads")
    public String threads(@RequestHeader(name = Htmx.CURRENT_URL, required = false) String currentUrl, Model model) {
        model.addAttribute("threads", imageThreadService.getAll());
        model.addAttribute("currentUrl", currentUrl == null ? "" : currentUrl);
        return "fragments/nav :: threads";
    }
}
