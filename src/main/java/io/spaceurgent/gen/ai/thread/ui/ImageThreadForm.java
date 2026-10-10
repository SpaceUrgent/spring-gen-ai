package io.spaceurgent.gen.ai.thread.ui;

import io.spaceurgent.gen.ai.image.application.dto.Orientation;
import io.spaceurgent.gen.ai.image.application.dto.StyleDetails;
import io.spaceurgent.gen.ai.thread.application.dto.ImageThreadRequest;
import io.spaceurgent.gen.ai.thread.domain.GenImageRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class ImageThreadForm {
    private UUID bookId;
    @NotBlank(message = "Describe what to illustrate")
    private String prompt;
    private String negativePrompt;
    private Orientation orientation = Orientation.LANDSCAPE;
    private String style;
    private String medium;
    private String colorPalette;
    private String detail;

    public static ImageThreadForm of(GenImageRequest request) {
        ImageThreadForm form = new ImageThreadForm();
        form.setPrompt(request.getPrompt());
        form.setNegativePrompt(request.getNegativePrompt());
        form.setOrientation(request.getOrientation() == null ? Orientation.LANDSCAPE : request.getOrientation());
        StyleDetails style = request.getStyle();
        if (style != null) {
            form.setStyle(style.style());
            form.setMedium(style.medium());
            form.setColorPalette(style.colorPalette());
            form.setDetail(style.detail());
        }
        return form;
    }

    public GenImageRequest toGenImageRequest() {
        GenImageRequest request = new GenImageRequest();
        request.setPrompt(prompt);
        request.setNegativePrompt(negativePrompt);
        request.setOrientation(orientation == null ? Orientation.LANDSCAPE : orientation);
        if (style != null || medium != null || colorPalette != null || detail != null) {
            request.setStyle(new StyleDetails(style, medium, colorPalette, detail));
        }
        return request;
    }

    public ImageThreadRequest toImageThreadRequest() {
        ImageThreadRequest request = new ImageThreadRequest();
        request.setBookId(bookId);
        request.setGenImageRequest(toGenImageRequest());
        return request;
    }
}
