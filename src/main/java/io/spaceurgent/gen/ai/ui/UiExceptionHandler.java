package io.spaceurgent.gen.ai.ui;

import io.spaceurgent.gen.ai.book.ui.BookUiController;
import io.spaceurgent.gen.ai.chat.ui.ChatUiController;
import io.spaceurgent.gen.ai.shared.exception.BadRequestException;
import io.spaceurgent.gen.ai.thread.ui.ImageThreadPageController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(assignableTypes = {
        LayoutController.class,
        BookUiController.class,
        ChatUiController.class,
        ImageThreadPageController.class
})
public class UiExceptionHandler {

    private static final String DEFAULT_ERROR_TARGET = "#page-alert";

    @ExceptionHandler
    String handle(Exception e, HttpServletRequest request, HttpServletResponse response, Model model) {
        HttpStatus status = status(e);
        if (status.is5xxServerError()) {
            log.error("UI request {} {} failed", request.getMethod(), request.getRequestURI(), e);
        } else {
            log.info("UI request {} {} rejected: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        }
        model.addAttribute("message", "Something went wrong. Please try again.");
        if (Htmx.isHtmx(request)) {
            String target = request.getHeader(Htmx.ERROR_TARGET);
            Htmx.retarget(response, StringUtils.hasText(target) ? target : DEFAULT_ERROR_TARGET);
            return "fragments/alert :: alert";
        }
        response.setStatus(status.value());
        model.addAttribute("status", status.value());
        return "error";
    }

    private static HttpStatus status(Exception e) {
        return switch (e) {
            case BadRequestException ignored -> HttpStatus.BAD_REQUEST;
            case ResponseStatusException rse -> HttpStatus.resolve(rse.getStatusCode().value()) instanceof HttpStatus s
                    ? s : HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
