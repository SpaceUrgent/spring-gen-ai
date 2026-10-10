package io.spaceurgent.gen.ai.image.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Locale;

public enum Orientation {
    LANDSCAPE,
    PORTRAIT,
    SQUARE;

    @JsonValue
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static Orientation of(String value) {
        return Arrays.stream(values())
                .filter(orientation -> orientation.name().equalsIgnoreCase(value == null ? "" : value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown orientation '%s', expected one of: landscape, portrait, square".formatted(value)));
    }
}
