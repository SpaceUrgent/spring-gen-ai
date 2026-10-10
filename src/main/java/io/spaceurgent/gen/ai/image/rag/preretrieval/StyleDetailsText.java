package io.spaceurgent.gen.ai.image.rag.preretrieval;

import io.spaceurgent.gen.ai.image.application.dto.StyleDetails;

import java.util.ArrayList;
import java.util.List;

public final class StyleDetailsText {
    private StyleDetailsText() {
    }

    private static final String STYLE = "style";
    private static final String MEDIUM = "medium";
    private static final String COLOR_PALETTE = "color palette";
    private static final String DETAIL = "detail";

    public static String format(StyleDetails details) {
        if (details == null) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        addIfText(lines, STYLE, details.style());
        addIfText(lines, MEDIUM, details.medium());
        addIfText(lines, COLOR_PALETTE, details.colorPalette());
        addIfText(lines, DETAIL, details.detail());
        return String.join("\n", lines);
    }

    private static void addIfText(List<String> lines, String name, String value) {
        if (isText(value)) {
            lines.add(name + ": " + value);
        }
    }

    private static boolean isText(String value) {
        return value != null && !value.isBlank();
    }
}
