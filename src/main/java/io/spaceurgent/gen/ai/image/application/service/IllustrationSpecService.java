package io.spaceurgent.gen.ai.image.application.service;

import io.spaceurgent.gen.ai.book.application.service.BookService;
import io.spaceurgent.gen.ai.shared.exception.BadRequestException;
import io.spaceurgent.gen.ai.image.application.dto.ImageRequest;
import io.spaceurgent.gen.ai.image.application.dto.ImageSpec;
import io.spaceurgent.gen.ai.image.application.dto.Orientation;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.spaceurgent.gen.ai.image.application.exception.IllustrationNotPossibleException.Reason.CHARACTERS_NOT_FOUND;
import static io.spaceurgent.gen.ai.image.application.exception.IllustrationNotPossibleException.Reason.NO_SUBJECT;
import io.spaceurgent.gen.ai.image.application.dto.ImageRequirements;
import io.spaceurgent.gen.ai.image.application.dto.NormalizedImageRequest;
import io.spaceurgent.gen.ai.image.application.dto.BookFacts;
import io.spaceurgent.gen.ai.image.application.dto.IllustrationQuery;
import io.spaceurgent.gen.ai.image.application.exception.IllustrationNotPossibleException;
import io.spaceurgent.gen.ai.image.rag.preretrieval.ImageRequestNormalizer;
import io.spaceurgent.gen.ai.image.rag.preretrieval.SceneIntentReader;
import io.spaceurgent.gen.ai.image.rag.retrieval.CharacterRetrieval;
import io.spaceurgent.gen.ai.image.rag.retrieval.SceneRetrieval;
import io.spaceurgent.gen.ai.image.rag.postretrieval.BookFactsCollector;
import io.spaceurgent.gen.ai.image.rag.generation.ImageSpecWriter;

@Service
@RequiredArgsConstructor
public class IllustrationSpecService {

    private final BookService bookService;
    private final ImageRequestNormalizer normalizer;
    private final SceneIntentReader intentReader;
    private final SceneRetrieval sceneRetrieval;
    private final CharacterRetrieval characterRetrieval;
    private final BookFactsCollector factsCollector;
    private final ImageSpecWriter specWriter;

    public ImageSpec create(ImageRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        requireBook(request.getBookId());

        NormalizedImageRequest normalized = normalizer.normalize(request);

        IllustrationQuery query = intentReader.read(request.getBookId(), normalized.prompt());
        if (!query.hasSubject()) {
            throw new IllustrationNotPossibleException(NO_SUBJECT);
        }

        SceneRetrieval.Result scene = sceneRetrieval.retrieve(query);
        List<Document> characterPassages = characterRetrieval.retrieve(scene.query());
        if (!query.sceneRequested() && characterPassages.isEmpty()) {
            throw new IllustrationNotPossibleException(CHARACTERS_NOT_FOUND);
        }

        BookFacts facts = factsCollector.collect(scene.query(), scene.passages(), characterPassages);
        return specWriter.write(scene.query(), facts, requirements(request, normalized));
    }

    private void requireBook(UUID bookId) {
        if (!bookService.existsById(bookId)) {
            throw new BadRequestException("Book not found");
        }
    }

    private static ImageRequirements requirements(ImageRequest request, NormalizedImageRequest normalized) {
        Orientation orientation = request.getOrientation() == null ? Orientation.LANDSCAPE : request.getOrientation();
        return new ImageRequirements(normalized.style(), orientation, normalized.negativePrompt());
    }
}
