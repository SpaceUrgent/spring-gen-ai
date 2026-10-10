package io.spaceurgent.gen.ai.book.application.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomPdfReader implements DocumentReader {
    private static final String FILENAME_METADATA = "filename";

    private final Resource resource;

    private final Map<String, Object> customMetadata = new HashMap<>();

    public CustomPdfReader(Resource resource) {
        this.resource = resource;
    }

    @Override
    public List<Document> get() {
        try {
            String document = pdfToText(this.resource);
            this.customMetadata.put(FILENAME_METADATA, getResourceIdentifier(this.resource));
            return List.of(new Document(document, this.customMetadata));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getResourceIdentifier(Resource resource) {
        String filename = resource.getFilename();
        if (filename != null && !filename.isEmpty()) {
            return filename;
        }

        try {
            URI uri = resource.getURI();
            return uri.toString();
        } catch (IOException ignored) {
        }

        try {
            URL url = resource.getURL();
            return url.toString();
        } catch (IOException ignored) {
        }

        return resource.getDescription();
    }

    private String pdfToText(Resource resource) throws IOException {
        try (PDDocument document = Loader.loadPDF(resource.getContentAsByteArray())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
