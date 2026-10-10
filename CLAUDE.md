# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

"Book Illustrator": a Spring Boot 4.1 / Spring AI 2.0 app (Java 25, Maven, Lombok, Thymeleaf + htmx UI) that indexes PDF books into pgvector and uses RAG to turn a short scene/character request into a structured `ImageSpec`. OpenAI then draws the image from that spec. Ollama (`qwen3:8b` for chat, `nomic-embed-text-v2-moe` for embeddings) does all LLM work, and OpenAI is used only for images. A book Q&A chat is included as a tool for testing retrieval. `README.md` explains the pipelines step by step for users.

## Commands

```bash
./mvnw compile
./mvnw test                                                   # all tests
./mvnw test -Dtest=GenAiApplicationTests#contextLoads         # single test
./mvnw spring-boot:run -Dspring-boot.run.profiles=local       # run against local Postgres + Ollama
docker compose up -d --build                                  # full stack: app, Postgres, Ollama, Prometheus, Loki, Grafana
docker compose -f deploy/pg/docker-compose.yml up -d          # only Postgres on :5432, for the local profile
```

The only test loads the full `@SpringBootTest` context, so Postgres and Ollama must be running. The Dockerfile builds with `-DskipTests` for that reason.

## Profiles and config

- `application.yaml`: shared settings (actuator/metrics, `app.chat.rag.*`).
- `application-docker.yml`: everything comes from env vars (`DB_*`, `OLLAMA_*`, `OPENAI_API_KEY`, `OPEN_AI_IMAGE_MODEL`, `ENABLE_CHAT_CLIENT_LOGGING`), which `docker-compose.yml` sets. The app container waits for `ollama-models` to pull both models.
- `application-local.yml` (gitignored, like everything under `/local` and `*-local.*`): localhost Postgres/Ollama, DEBUG logging for Spring AI. `OPENAI_API_KEY` comes from the environment.
- One of these profiles is required, because datasource and model settings exist only there.
- `spring.ai.model.*` selectors pin chat/embedding to Ollama and image to OpenAI. Adding the OpenAI starter also registers OpenAI chat/embedding auto-configs, so these selectors must stay.
- `schema.sql` runs on every startup and creates `vector_store` with `vector(768)`, which must match the embedding model's dimensions. JPA tables use Hibernate `ddl-auto: update`.
- RAG tuning lives in `@ConfigurationProperties` records with defaults: `app.chat.rag` (`ChatRagProperties`), `app.image.rag` (`ImageRagProperties`), and `app.image.spec` (`ImageSpecProperties`, structured-output retry count).

## Architecture

Package root `io.spaceurgent.gen.ai`, organized by feature: `book`, `chat`, `image`, `thread`, plus `shared`, `ai`, `ui`, `config`. Each feature uses `api/` (JSON REST under `/api/...`), `ui/` (Thymeleaf/htmx page controllers), `application/` (services, DTOs, exceptions), `domain/` (JPA entities + repositories), and `rag/`. Inside `rag/`, subpackages follow the Spring AI modular RAG stages: `preretrieval/`, `retrieval/`, `postretrieval/`, `generation/`.

**Ingestion** (`book/`): `UploadBookService` saves a `Book`, extracts text with PDFBox (`CustomPdfReader`), splits it with `TokenTextSplitter` into roughly 100-token chunks, and tags each chunk with `bookId`, `bookTitle`, and `chunkIndex` (keys in `shared/rag/BookMetadata`) before writing to `VectorStore`. Several later steps depend on this metadata:
- Every search is scoped by a `bookId` filter.
- `BookChunkRepository` queries `vector_store` with raw JDBC on `metadata->>'chunkIndex'` to load chunks that neighbor a hit (context-window expansion).

If you change the metadata keys, update every reader.

**Illustration pipeline** (`image/`, entry point `IllustrationSpecService.create`). This is not a `RetrievalAugmentationAdvisor`. The service calls each stage explicitly:
1. `ImageRequestNormalizer` translates the request into an English `NormalizedImageRequest`.
2. `SceneIntentReader` separates story terms from picture terms and produces an `IllustrationQuery`. The request fails with `IllustrationNotPossibleException` if it has no subject.
3. `SceneRetrieval` uses `SceneQueryExpander` (multi-query), runs `PassageSearch` per query, and merges results with `RoundRobinDocumentJoiner`. `SceneCharacterFinder` then extracts the characters in the scene.
4. `CharacterRetrieval` runs one appearance query per character.
5. `BookFactsCollector` uses `NeighborChunksPostProcessor` and `VisualFactsCondenser` to produce `BookFacts` under the CHARACTERS, SETTING, OBJECTS, ATMOSPHERE, and EVENT headings.
6. `ImageSpecWriter` produces the `ImageSpec` (structured output).

Structured-output steps use `.entity(X.class)` with `StructuredOutputValidationAdvisor`, which retries on schema mismatch. `ImageSpecConfig` defines two named `ChatClient` beans: `FAST_CHAT_CLIENT`, which disables qwen3 thinking, and `CHAT_CLIENT`, which keeps thinking enabled. Inject them by qualifier.

**Image generation and threads**: `ImageGenerationService` sends the spec to `OpenAiImageModel`, maps `Orientation` to a size, and stores the PNG bytes in Postgres (`Image` entity, served by `ImageService`). `thread/ImageThreadService` is the user-facing orchestrator. An `ImageThread` belongs to a `Book` and holds the current `GenImageRequest`. Each create/regenerate runs spec + image and appends an `ImageGenMetadata` row (request, spec, model, image). The first generation renames the thread to the spec's title.

**Chat** (`chat/`): `PersistentMemoryRagChatService` builds one `ChatClient` with `MessageChatMemoryAdvisor` (backed by `JpaChatMemory`, using the `Chat`/`ChatMessage` entities) plus a `RetrievalAugmentationAdvisor` from `IterativeChatRagAdvisorFactory`. `QueryLanguageDetector` runs before the call and passes `ENGLISH_QUERY`/`LANGUAGE` as advisor params (keys in `ChatRagContext`). The advisor's stages run in this order:
1. `TranslatedQueryTransformer`
2. `HistoryCompressionQueryTransformer`, which turns follow-up questions into standalone ones
3. `RewriteQueryTransformer`
4. `IterativeBookRetriever`: one search with the full question, then a tool-calling agent that calls `BookSearchTools.searchPassages` 2 to `maxSearches` times and collects the results into `RetrievedDocuments` through tool context
5. `ExplainingQueryAugmenter`

The answer is given in the user's language.

### Conventions worth knowing

- Helper LLM calls always get their own `chatClientBuilder.clone()`, usually through `ChatClientBuilders.withoutThinking(...)`, so advisors on the main client (memory, RAG, logging) don't leak into them. `ChatClientConfig` adds `SimpleLoggerAdvisor` to every builder when `chat-client.enable-logging` is true.
- Prompts are inline Java text blocks next to the code that uses them. They are deliberately strict: preserve proper names and fictional terms, never "correct" ambiguous words, and never use outside knowledge about the book.
- UI controllers use the `ui/Htmx` helpers (`HX-Redirect`, `HX-Retarget`, `requireValid`). `UiExceptionHandler` handles errors for pages and `shared/api/ApiExceptionHandler` handles them for the REST API.
- Observability: `management.metrics.distribution.percentiles-histogram` is enabled for Spring AI observations, `ImageGenerationMetricsHandler` adds image metrics, and logs go to Loki via `logback-spring.xml`. The Grafana dashboard is in `deploy/observability/`.

## Behavioral guidelines

These guidelines aim to reduce common LLM coding mistakes. They favor caution over speed, so use judgment on trivial tasks.

### 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

### 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

In this project the only existing test needs Postgres and Ollama running. When those aren't available, verify at least with `./mvnw compile`, and say which checks you actually ran.
