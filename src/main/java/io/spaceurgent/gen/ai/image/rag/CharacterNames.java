package io.spaceurgent.gen.ai.image.rag;

import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CharacterNames {
    private CharacterNames() {
    }

    public static List<String> clean(@Nullable List<String> names) {
        return names == null ? List.of() : merge(names, List.of(), Integer.MAX_VALUE);
    }

    public static List<String> merge(List<String> first, @Nullable List<String> second, int max) {
        List<String> all = new ArrayList<>(first);
        if (second != null) {
            all.addAll(second);
        }
        Map<String, String> unique = new LinkedHashMap<>();
        all.stream()
                .filter(StringUtils::hasText)
                .map(String::strip)
                .forEach(name -> unique.putIfAbsent(name.toLowerCase(Locale.ROOT), name));
        return unique.values().stream().limit(max).toList();
    }
}
