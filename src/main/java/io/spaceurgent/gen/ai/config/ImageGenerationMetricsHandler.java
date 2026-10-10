package io.spaceurgent.gen.ai.config;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.image.observation.ImageModelObservationContext;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ImageGenerationMetricsHandler implements ObservationHandler<ImageModelObservationContext> {

    static final String METRIC_NAME = "app.images.generated";

    private final MeterRegistry meterRegistry;

    @Override
    public void onStop(ImageModelObservationContext context) {
        ImageResponse response = context.getResponse();
        if (response == null || response.getResults().isEmpty()) {
            return;
        }
        Counter.builder(METRIC_NAME)
                .description("Number of generated images")
                .tags(tags(context))
                .register(meterRegistry)
                .increment(response.getResults().size());
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof ImageModelObservationContext;
    }

    private static List<Tag> tags(ImageModelObservationContext context) {
        List<Tag> tags = new ArrayList<>();
        for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
            tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
        }
        ImageOptions options = context.getRequest().getOptions();
        String size = null;
        String quality = null;
        if (options instanceof OpenAiImageOptions openAiOptions) {
            size = openAiOptions.getSize();
            quality = openAiOptions.getQuality();
        }
        if (size == null && options != null && options.getWidth() != null && options.getHeight() != null) {
            size = options.getWidth() + "x" + options.getHeight();
        }
        tags.add(Tag.of("size", size == null ? "default" : size));
        tags.add(Tag.of("quality", quality == null ? "default" : quality));
        return tags;
    }
}
