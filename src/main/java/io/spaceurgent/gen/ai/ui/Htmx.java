package io.spaceurgent.gen.ai.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

public final class Htmx {
    private Htmx() {
    }

    public static final String REQUEST = "HX-Request";
    public static final String CURRENT_URL = "HX-Current-URL";
    public static final String ERROR_TARGET = "X-Error-Target";

    public static boolean isHtmx(HttpServletRequest request) {
        return "true".equals(request.getHeader(REQUEST));
    }

    public static void redirect(HttpServletResponse response, String location) {
        response.setHeader("HX-Redirect", location);
    }

    public static void trigger(HttpServletResponse response, String event) {
        response.setHeader("HX-Trigger", event);
    }

    public static void retarget(HttpServletResponse response, String target) {
        response.setHeader("HX-Retarget", target);
        response.setHeader("HX-Reswap", "innerHTML");
    }

    public static void requireValid(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String message = bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .distinct()
                    .collect(Collectors.joining(". "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
