package io.spaceurgent.gen.ai.image.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.image.spec")
public record ImageSpecProperties(
        @DefaultValue("2") int maxRepeatAttempts
) {
}
