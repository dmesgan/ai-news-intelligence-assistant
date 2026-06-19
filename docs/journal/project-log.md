# Project Log — AI News Intelligence Assistant

A running journal of work completed each session. Updated at the end of every working session to preserve context across restarts.

---

## 2026-06-13

### What was done
- Created the full documentation structure for the project from scratch.
- Generated `docs/requirements/project-vision.md` — goals, user stories, functional and non-functional requirements, category taxonomy, success metrics.
- Generated `docs/architecture/system-overview.md` — component diagram, package structure, data model, tech stack table, data flow, AWS migration path.
- Generated `docs/sprints/sprint-plan.md` — 8-sprint delivery roadmap with tasks, verification steps, milestones, and risk register.
- Generated root `README.md` — project overview, quick start, structure, environment variables, sprint status table.
- Created `CLAUDE.md` at project root — defines tech stack, architecture principles, coding rules, package structure, DB design, sprint roadmap, and AI assistant communication rules.
- Updated all docs to align with the correct tech stack (Java 21 + Spring Boot 3 + Maven) after initial docs incorrectly used Python/FastAPI.
- Created `.gitignore` — excludes `.idea/`, `CLAUDE.md`, `prompt.md`.
- Created `prompt.md` at project root — short prompt reminder for AI assistant communication style.
- Committed `.gitignore`, `README.md`, and all three docs to `main` (commit `b68d9a2`).

### Current state
- Documentation complete and consistent.
- No application code exists yet.
- Git repo is clean on `main`, up to date with `origin/main`.
- `CLAUDE.md` and `prompt.md` exist locally but are gitignored.

### Current sprint
**Sprint 1 — Backend Foundation**

Next steps:
- Create Spring Boot 3 project (Java 21, Maven) inside `backend/`
- Add dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, Lombok
- Write `docker-compose.yml` with `db` and `backend` services
- Create `Article`, `Summary`, `DailyDigest` entities with UUID PKs
- Create repositories, services, controllers, DTOs, mappers
- Wire up `GlobalExceptionHandler`

### Tech stack decided
- Backend: Java 21, Spring Boot 3, Maven
- Database: PostgreSQL 16
- ORM: Spring Data JPA
- Scheduling: Spring `@Scheduled`
- LLM: Ollama + Llama 3.1 (local, no external API)
- Frontend: React 18, TypeScript, Vite, Tailwind CSS, Axios
- Future: pgvector, Claude API / OpenAI API, AWS (EC2, RDS, S3, CloudFront, SES)

### Package structure
```
com.mesgan.ainews
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── client
├── scheduler
├── config
└── exception
```

### Key decisions recorded
- UUID primary keys on all entities (not SERIAL/auto-increment)
- Summaries stored in a separate `summaries` table (not embedded in articles)
- `processed` boolean on Article (not a status enum) for v1 simplicity
- Constructor injection only (`@RequiredArgsConstructor`), never `@Autowired`
- DTOs always returned from controllers — entities never exposed directly

---

## 2026-06-19

### What was done
- Generated all Sprint 1 backend code — 26 files created.
- `backend/pom.xml` — Spring Boot 3.3.4, Java 21, dependencies: Web, Data JPA, Validation, PostgreSQL driver, Lombok.
- Entities: `Article`, `Summary`, `DailyDigest` — UUID PKs via `@GeneratedValue(strategy = GenerationType.UUID)`, `@Builder.Default` for field defaults, Lombok `@Data/@Builder`.
- Repositories: `ArticleRepository`, `SummaryRepository`, `DailyDigestRepository` — extend `JpaRepository`, custom derived queries (`existsByUrl`, `countByProcessedTrue/False`, `findByArticleId`, `findByDigestDate`) plus JPQL `@Query` for keyword search.
- DTOs: Java records (`ArticleResponse`, `ArticleDetailResponse`, `DigestResponse`, `StatusResponse`) — immutable, no boilerplate.
- Mappers: `ArticleMapper`, `DigestMapper` — plain `@Component` classes, accept entity + optional Summary, return records. Summary fields null-safe.
- Services: `NewsService`, `DigestService` — `@Transactional(readOnly = true)`, `@RequiredArgsConstructor`, `@Slf4j`. No business logic leaked to controllers.
- Controllers: `NewsController` (`/api/news/latest`, `/api/news/search`, `/api/news/{id}`), `DigestController` (`/api/digest/today`, `/api/digest/{date}`), `StatusController` (`/api/health`, `/api/status`).
- Exception: `ResourceNotFoundException` → 404, `MethodArgumentNotValidException` → 400, catch-all → 500. All via `GlobalExceptionHandler` (`@RestControllerAdvice`).
- `WebConfig` — CORS for `localhost:5173` and `localhost:3000`.
- `schema.sql` — creates `articles`, `summaries`, `daily_digests` tables with `IF NOT EXISTS` guards and all indexes.
- `application.yml` — all config via env vars with defaults, `ddl-auto=validate`, `sql.init.mode=always`.
- `Dockerfile` — multi-stage build (Maven builder + JRE runtime).
- `docker-compose.yml` — `db` (PostgreSQL 16) + `backend` services, health check on db.
- `.env.example` — all required environment variables.

### Current state
- Sprint 1 complete. All backend layers implemented. No data in DB yet.
- To test: `docker-compose up` → insert a row manually in `articles` → `GET /api/news/latest` returns it as DTO.
- `GET /api/health` → `{"status":"UP"}`
- `GET /api/status` → article counts
- `GET /api/news/{bad-uuid}` → `{"status":404,"message":"...","timestamp":"..."}`

### Current sprint
**Sprint 2 — News Ingestion** (not started)

Next steps:
- Add `WebClient` dependency to `pom.xml`
- Implement `GdeltClient` — HTTP call to GDELT 2.0, CSV parsing
- Implement `NewsIngestionService` — deduplication by URL, batch insert
- Implement `GdeltCollectorScheduler` with `@Scheduled`
- Add `POST /api/admin/trigger-ingest` manual trigger endpoint

### Key design decisions
- `Summary.articleId` is a plain `UUID` column (not a JPA `@OneToOne`) — matches CLAUDE.md spec, avoids lazy-loading complexity
- Services call `summaryRepository.findByArticleId()` per article in list view — acceptable in Sprint 1 (no summaries exist yet); optimize with JOIN in Sprint 3
- `@RestControllerAdvice` used (not `@ControllerAdvice`) — combines advice + `@ResponseBody`, correct for REST APIs
- Java records for DTOs — immutable, Jackson serializes them automatically

---

## 2026-06-19 (continued — tests, IntelliJ setup, app verified running)

### What was done
- Added 38 test cases across 8 test classes: `NewsServiceTest` (7), `DigestServiceTest` (4), `ArticleMapperTest` (4), `DigestMapperTest` (1), `NewsControllerTest` (5), `DigestControllerTest` (4), `StatusControllerTest` (3), `ArticleRepositoryTest` (7), `AiNewsApplicationTests` (1).
- Added H2 dependency to `pom.xml` (test scope) for in-memory database in repository and context load tests.
- Fixed test failure: `ArticleRepositoryTest` was failing with `SchemaManagementException: missing table [articles]` because `application.yml` sets `ddl-auto=validate` which conflicts with H2 (no tables). Fix: added `spring.jpa.hibernate.ddl-auto=create-drop` to `@TestPropertySource` so Hibernate creates tables from entities.
- Fixed `application.yml`: removed hardcoded `hibernate.dialect: PostgreSQLDialect` — Hibernate 6 auto-detects the dialect; having it explicit broke H2 tests.
- Set up IntelliJ run configuration: `backend/.run/AiNewsApplication.run.xml` activates the `local` Spring profile.
- Created `application-local.yml` — local profile with `localhost:5432`, SQL logging enabled.
- Added Maven wrapper: `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` (Maven 3.9.9).
- Added `AiNewsApplicationTests.java` — context load smoke test using H2.
- Updated `.gitignore` to exclude `backend/target/`, `out/` (IntelliJ build output), `.env`.
- Verified the app builds and runs:
  - `./mvnw clean compile` → BUILD SUCCESS (22 source files)
  - `./mvnw test` → 38 tests, 0 failures
  - `docker-compose up --build` → both `db` and `backend` containers running
  - `GET /api/health` → `{"status":"UP"}`
  - `GET /api/status` → `{"totalArticles":0,...}`
  - Manual article insert → `GET /api/news/latest` returns article as JSON
  - `GET /api/news/search?keyword=Spring` → returns matching article
  - `GET /api/news/{id}` → full detail response
  - `GET /api/news/00000000-...` → `{"status":404,...}`

### Current state
- Sprint 1 **complete and verified**.
- App runs from IntelliJ: `docker-compose up db` for database, then run `AiNewsApplication` with profile `local`.
- App runs from Docker: `docker-compose up --build` (starts both db + backend on port 8080).
- All 38 tests pass: `./mvnw test`
- **Port conflict note:** Do not run IntelliJ and `docker-compose up` (full) at the same time — both bind port 8080. Use `docker-compose up db` when running from IntelliJ.

### Current sprint
**Sprint 2 — News Ingestion** (not started)

Next steps:
- Add `spring-boot-starter-webflux` to `pom.xml` for `WebClient`
- Implement `GdeltClient` — HTTP call to GDELT 2.0, CSV parsing
- Implement `NewsIngestionService` — deduplication by URL, batch insert
- Implement `GdeltCollectorScheduler` with `@Scheduled`
- Add `POST /api/admin/trigger-ingest` manual trigger endpoint

### Bugs fixed
- `hibernate.dialect` in `application.yml` caused H2 test failures — removed (Hibernate 6 auto-detects)
- `ddl-auto=validate` with H2 (no tables) caused context load failure — fixed with `create-drop` in test `@TestPropertySource`
- Port 8080 conflict when running IntelliJ + full docker-compose simultaneously — resolved by using `docker-compose up db` only

---

## 2026-06-19 (Sprint 2 — GDELT News Ingestion)

### What was done
- Added `spring-boot-starter-webflux` to `pom.xml` — provides `WebClient` for HTTP calls.
- `config/WebClientConfig.java` — `@Bean` WebClient with 50MB buffer for GDELT ZIP downloads.
- `config/SchedulingConfig.java` — `@EnableScheduling` to activate `@Scheduled` jobs.
- `client/GdeltClient.java` — fetches `lastupdate.txt`, identifies latest GKG ZIP URL, downloads and unzips in memory, parses tab-separated GKG v2 format, returns `List<GdeltArticleDto>`.
- `dto/GdeltArticleDto.java` — internal record (url, sourceName, themes, tone, publishedAt). Never returned to API clients.
- `dto/IngestionResultDto.java` — API response record (articlesFound, articlesNew, articlesDuplicate, durationMs, status).
- `service/NewsIngestionService.java` — deduplicates by URL (`existsByUrl`), maps GDELT themes → category taxonomy, batch-saves new articles, returns stats.
- `scheduler/GdeltCollectorScheduler.java` — `@Scheduled(fixedDelay)` runs `ingestLatest()` every 15 minutes. Fixed delay prevents overlap.
- `controller/NewsController.java` — added `POST /api/news/fetch` manual trigger endpoint.
- `application.yml` + `application-local.yml` — added `gdelt.collect-interval-ms` config.
- Fixed `NewsControllerTest` — added `@MockBean NewsIngestionService` after adding it to the controller.

### GDELT integration details
- Source file: `http://data.gdeltproject.org/gdeltv2/lastupdate.txt` → line 3 = GKG ZIP URL
- GKG v2 fields used: index 1 (DATE), 3 (SourceCommonName), 4 (DocumentIdentifier/URL), 8 (V2Themes), 15 (V21Tone)
- Category mapped from themes prefix matching (ECON_ → Economy, CYBER → Cybersecurity, etc.)
- Title/description = null in Sprint 2; Ollama will generate them in Sprint 3
- `POST /api/news/fetch` — no request body, returns ingestion stats JSON

### Current state
- Sprint 2 complete.
- `POST /api/news/fetch` → triggers live GDELT ingestion, returns `{articlesFound, articlesNew, articlesDuplicate, durationMs, status}`
- `GET /api/news/latest` → returns real GDELT articles from DB
- Scheduler runs automatically every 15 minutes once app starts
- All 38 tests pass: `./mvnw test`

### Current sprint
**Sprint 3 — AI Summaries** (not started)

Next steps:
- Add Ollama client (`OllamaClient`) using WebClient
- Implement `SummaryService` — calls Ollama with article URL, stores result in `summaries` table
- Implement `AiProcessorScheduler` — picks up `processed=false` articles in batches
- Update `GET /api/news/latest` and `GET /api/news/{id}` to include summary fields

### Key design decisions
- `fixedDelay` (not `fixedRate`) for scheduler — next run starts 15 min AFTER previous completes, preventing overlap on slow networks
- GKG file used over export.CSV — GKG is article-level (one row = one document); export.CSV is event-level
- `WebClient.block()` is acceptable here — runs on scheduler thread (not a reactive pipeline)
- `@MockBean NewsIngestionService` needed in `NewsControllerTest` — any new dependency added to a controller must be mocked in its `@WebMvcTest` test class

---
