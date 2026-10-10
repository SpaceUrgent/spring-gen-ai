package io.spaceurgent.gen.ai.image.application.exception;

import lombok.Getter;

@Getter
public class IllustrationNotPossibleException extends RuntimeException {

    public enum Reason {
        NO_SUBJECT("The request names no scene and no character from the book. Please describe a scene or a character."),
        SCENE_NOT_FOUND("The requested scene was not found in the book. Please describe it in more detail."),
        CHARACTERS_NOT_FOUND("The requested characters were not found in the book. Please check the names.");

        private final String message;

        Reason(String message) {
            this.message = message;
        }
    }

    private final Reason reason;

    public IllustrationNotPossibleException(Reason reason) {
        super(reason.message);
        this.reason = reason;
    }
}
