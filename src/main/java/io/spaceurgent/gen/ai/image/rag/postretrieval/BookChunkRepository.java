package io.spaceurgent.gen.ai.image.rag.postretrieval;

import io.spaceurgent.gen.ai.shared.rag.BookMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BookChunkRepository {

    private static final String SQL = """
            SELECT id, content, (metadata->>'chunkIndex')::int AS chunk_index
            FROM vector_store
            WHERE metadata->>'bookId' = :bookId
              AND (metadata->>'chunkIndex')::int IN (:chunkIndexes)
            ORDER BY chunk_index
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<Document> findByChunkIndexes(String bookId, Collection<Integer> chunkIndexes) {
        if (chunkIndexes.isEmpty()) {
            return List.of();
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("bookId", bookId)
                .addValue("chunkIndexes", chunkIndexes);
        return jdbcTemplate.query(SQL, parameters, (rs, rowNum) -> Document.builder()
                .id(rs.getString("id"))
                .text(rs.getString("content"))
                .metadata(Map.of(
                        BookMetadata.BOOK_ID, bookId,
                        BookMetadata.CHUNK_INDEX, rs.getInt("chunk_index")))
                .build());
    }
}
