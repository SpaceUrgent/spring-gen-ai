package io.spaceurgent.gen.ai.image.application.dto;

public record BookFacts(String text) {

    public static final BookFacts NONE = new BookFacts("");
}
