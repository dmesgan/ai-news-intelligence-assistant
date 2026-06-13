# System Architecture — AI News Intelligence Assistant

## 1. Architecture Style

**Layered Monolith with scheduled workers (v1) → Services on AWS (v2)**

In v1, all backend components run within a single Spring Boot application using Spring's built-in `@Scheduled` task execution. This keeps local deployment simple (`docker-compose up`) while enforcing clean internal boundaries — Controller → Service → Repository — that map naturally to separate AWS services in v2.

The architecture follows **Clean Architecture**:
- No business logic in controllers
- Constructor injection only (`@RequiredArgsConstructor`)
- Entities never returned directly from controllers (DTO pattern)
- UUID primary keys on all entities
- Global exception handling via `@ControllerAdvice`

---

## 2. High-Level Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        USER'S MACHINE (v1)                   │
│                                                              │
│  ┌──────────────┐     ┌──────────────────────────────────┐  │
│  │  React UI    │────▶│      Spring Boot Backend          │  │
│  │  (Vite/TS)   │◀────│  /api/news  /api/digest           │  │
│  └──────────────┘     └────────┬─────────────────────────┘  │
│                                │                             │
│              ┌─────────────────┼─────────────────┐          │
│              │                 │                 │           │
│              ▼                 ▼                 ▼           │
│  ┌──────────────────┐ ┌──────────────┐ ┌──────────────────┐ │
│  │  GDELT Collector │ │  AI Processor│ │  Digest Generator│ │
│  │  (@Scheduled)    │ │  (@Scheduled)│ │  (@Scheduled)    │ │
│  └────────┬─────────┘ └──────┬───────┘ └────────┬─────────┘ │
│           │                  │                  │            │
│           ▼                  ▼                  │            │
│  ┌──────────────────────────────────────────┐   │           │
│  │           PostgreSQL Database             │◀──┘           │
│  │  articles | summaries | daily_digests    │               │
│  └──────────────────────────────────────────┘               │
│                             │                               │
│                             ▼                               │
│                   ┌──────────────────┐                      │
│                   │   Ollama (local) │                      │
│                   │   Llama 3.1      │                      │
│                   └──────────────────┘                      │
└─────────────────────────────────────────────────────────────┘
         │ GDELT 2.0 HTTP endpoint (external, read-only)
         ▼
    api.gdeltproject.org
```

---

## 3. Package Structure

```
com.mesgan.ainews
├── controller      # REST controllers — routing and request/response only
├── service         # Business logic — all processing lives here
├── repository      # Spring Data JPA repositories
├── entity          # JPA entities (mapped to DB tables)
├── dto             # Request and response DTOs (never expose entities)
├── mapper          # Entity ↔ DTO conversion
├── client          # External HTTP clients (GDELT, Ollama)
├── scheduler       # @Scheduled job classes
├── config          # Spring configuration classes
└── exception       # GlobalExceptionHandler + custom exceptions
```

---

## 4. Component Breakdown

### 4.1 GDELT Collector

- **Responsibility:** Polls the GDELT 2.0 API every 15 minutes, parses event CSV, deduplicates by URL, and persists new records to the `articles` table with `processed = false`.
- **Technology:** Java, Spring `@Scheduled`, `WebClient` for HTTP, manual CSV parsing.
- **Location:** `scheduler/GdeltCollectorScheduler.java` → `service/NewsIngestionService.java` → `client/GdeltClient.java`
- **Output:** `Article` rows with `processed = false`.

**Why GDELT 2.0:** Free, no API key, updates every 15 minutes, includes tone scores, geographic tags, and source metadata — exactly the signals needed for importance scoring.

---

### 4.2 AI Processor

- **Responsibility:** Picks up unprocessed articles (`processed = false`), calls Ollama for summarization, categorization, and importance scoring, writes a `Summary` record, marks the article `processed = true`.
- **Technology:** Java, `WebClient` to call Ollama REST API (`POST /api/generate`), Jackson for JSON parsing.
- **Location:** `scheduler/AiProcessorScheduler.java` → `service/SummaryService.java` → `client/OllamaClient.java`
- **Trigger:** `@Scheduled` every 2 minutes, processes up to N articles per cycle (configurable).
- **Prompt output (JSON):** `oneSentenceSummary`, `keyPoints`, `whyItMatters`, `aiCategory`, `importanceScore`

**Why Ollama:** Runs entirely offline, model-agnostic, exposes a simple REST API. Llama 3.1 is the default; the model name is configurable via environment variable. Future: swap for Claude API or OpenAI API without changing service logic.

---

### 4.3 Ranking / Importance Scoring

- **Responsibility:** Assigns an `importanceScore` (1–100) to each article as part of the AI prompt response.
- **Location:** `service/SummaryService.java` (parsed from Ollama response, stored on `Article` entity).
- **Note:** Score is AI-generated, not formula-based. The LLM reasons about geopolitical significance, novelty, and impact to produce the score. This is revisable in future sprints.

---

### 4.4 Digest Generator

- **Responsibility:** Once per day, selects the top-scored articles from the past 24 hours grouped by category, calls Ollama to write a cohesive narrative digest, stores it in `daily_digests`.
- **Location:** `scheduler/DigestScheduler.java` → `service/DigestService.java` → `client/OllamaClient.java`
- **Trigger:** `@Scheduled` cron at 07:00 local time (configurable).

---

### 4.5 Spring Boot REST API

- **Responsibility:** Exposes REST endpoints consumed by the React dashboard.
- **Technology:** Java 21, Spring Boot 3, Spring Web, Spring Data JPA, Bean Validation, Lombok.
- **Key endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/news/latest` | Paginated articles sorted by importance score |
| GET | `/api/news/search` | Keyword search across title and description |
| GET | `/api/news/{id}` | Single article with full summary detail |
| GET | `/api/digest/today` | Today's daily digest |
| GET | `/api/digest/{date}` | Digest for a specific date |
| GET | `/api/status` | Last ingestion time, processed/unprocessed counts |

- **Error handling:** `GlobalExceptionHandler` (`@ControllerAdvice`) handles all exceptions uniformly.
- **Validation:** Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Size`) on all DTOs.
- **Logging:** SLF4J throughout — no `System.out.println`.

---

### 4.6 React Dashboard

- **Responsibility:** Displays ranked news, supports category filtering and keyword search, shows article detail with full AI summary, shows daily digest.
- **Technology:** React 18, TypeScript, Vite, Tailwind CSS, Axios (HTTP client).
- **Architecture:** SPA served from `localhost:5173` in dev; built static assets served by Nginx in production.

**Why Axios over Fetch:** Consistent interceptor support for error handling and request/response transformation — cleaner than raw fetch for a structured API client layer.

---

## 5. Data Model

```sql
-- Core article store
CREATE TABLE articles (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title            TEXT,
    url              TEXT UNIQUE NOT NULL,
    source_name      TEXT,
    description      TEXT,
    content          TEXT,
    category         TEXT,
    importance_score INTEGER DEFAULT 0,
    processed        BOOLEAN DEFAULT FALSE,
    published_at     TIMESTAMPTZ,
    fetched_at       TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_articles_processed      ON articles(processed);
CREATE INDEX idx_articles_importance     ON articles(importance_score DESC);
CREATE INDEX idx_articles_published      ON articles(published_at DESC);
CREATE INDEX idx_articles_category       ON articles(category);

-- AI-generated summaries (separate table, one-to-one with articles)
CREATE TABLE summaries (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id           UUID NOT NULL REFERENCES articles(id),
    one_sentence_summary TEXT,
    key_points           TEXT,
    why_it_matters       TEXT,
    ai_category          TEXT,
    created_at           TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_summaries_article_id ON summaries(article_id);

-- Daily digest store
CREATE TABLE daily_digests (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    digest_date  DATE UNIQUE NOT NULL,
    content      TEXT NOT NULL,
    created_at   TIMESTAMPTZ DEFAULT NOW()
);

-- Future: pgvector embeddings table (Sprint 6)
-- CREATE TABLE article_embeddings (
--     article_id  UUID PRIMARY KEY REFERENCES articles(id),
--     embedding   vector(768)
-- );
```

**Key design decisions:**
- UUID PKs on all tables (no serial integers) — portable across environments, safe for future distributed deployment.
- Summaries in a separate table — keeps `articles` lean and queryable without pulling large text fields.
- `processed` boolean on `Article` — simpler than a status enum for v1; sufficient for the two-state workflow (unprocessed → processed).
- `daily_digests` (not `digests`) — explicit name avoids SQL keyword collision.

---

## 6. Technology Stack

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Language (backend) | Java 21 | LTS release; virtual threads; mature Spring ecosystem |
| Web framework | Spring Boot 3 | Industry standard; auto-configuration; built-in scheduling |
| Build tool | Maven | Dependency management; standard in Java enterprise projects |
| ORM | Spring Data JPA | Repository pattern out of the box; declarative queries |
| HTTP client | Spring WebClient | Non-blocking HTTP for Ollama and GDELT calls |
| Validation | Bean Validation | `@NotBlank`, `@NotNull`, `@Size` — declarative, standard |
| Boilerplate reduction | Lombok | `@RequiredArgsConstructor`, `@Data`, `@Builder` |
| Logging | SLF4J + Logback | Standard Java logging; no `System.out.println` |
| Database | PostgreSQL 16 | Reliable; UUID support; future pgvector extension |
| LLM runtime | Ollama | Local, model-agnostic REST API |
| Default LLM model | Llama 3.1 | Good summarization quality; configurable |
| Future LLM | Claude API / OpenAI API | Drop-in replacement via `OllamaClient` abstraction |
| Frontend framework | React 18 + TypeScript | Industry standard; strong ecosystem |
| Build tool (frontend) | Vite | Fast HMR; simple config |
| UI/Styling | Tailwind CSS | Utility-first; no design system overhead in v1 |
| HTTP client (frontend) | Axios | Consistent error handling; interceptor support |
| Containerization | Docker + docker-compose | Single-command local run; maps directly to ECS in v2 |
| Future: semantic search | pgvector | Vector similarity search within PostgreSQL |

---

## 7. Data Flow

```
[GDELT API] ──HTTP──▶ [GdeltClient] ──▶ [NewsIngestionService]
                                                  │
                                         INSERT articles
                                         (processed=false)
                                                  │
[AiProcessorScheduler every 2min] ──SELECT unprocessed──▶ [PostgreSQL]
        │
        ▼
[OllamaClient POST /api/generate]
        │
        ▼
{oneSentenceSummary, keyPoints, whyItMatters, aiCategory, importanceScore}
        │
        ▼
[SummaryService] ──INSERT summaries
                ──UPDATE articles SET processed=true, importance_score=N

[React UI] ──GET /api/news/latest──▶ [NewsController]
                                           │
                                    [NewsService]
                                           │
                                    [ArticleRepository]
                                           │
                              SELECT + JOIN summaries ORDER BY importance_score
                                           │
                              [ArticleMapper: Entity → DTO]
                                           │
                              [JSON response] ──▶ [React renders cards]

[DigestScheduler 07:00] ──▶ [DigestService] ──SELECT top-N──▶ [PostgreSQL]
                                   │──▶ [OllamaClient] ──▶ narrative digest
                                   └──INSERT daily_digests──▶ [PostgreSQL]
```

---

## 8. AWS Migration Path (v2)

| v1 Component | AWS Equivalent |
|-------------|----------------|
| PostgreSQL (local container) | Amazon RDS (PostgreSQL) |
| Ollama (local) | EC2 GPU instance with Ollama, or Claude API / Bedrock |
| Spring `@Scheduled` | Amazon EventBridge Scheduler + ECS Tasks |
| Spring Boot (local container) | ECS Fargate (behind ALB) |
| React SPA (local) | S3 + CloudFront |
| docker-compose | ECS Task Definitions |
| `.env` file | AWS Secrets Manager / Parameter Store |
| Email digest (local log) | Amazon SES |

The Clean Architecture layer boundaries defined in v1 map directly to ECS services in v2. Service interfaces remain unchanged — only infrastructure wiring changes.

---

## 9. Security Considerations (v1)

- Dashboard runs on localhost only; no public exposure in v1.
- No credentials stored in code; all via `.env` file (gitignored).
- Ollama bound to localhost only.
- PostgreSQL accessible only within the Docker network.
- GDELT is a public read-only API; no API key required.

---

## 10. Open Architecture Decisions

| Decision | Options | Current Choice | Revisit When |
|----------|---------|---------------|--------------|
| LLM model | Llama 3.1, Mistral 7B, Gemma | Llama 3.1 | Quality issues arise |
| Importance scoring | AI-generated vs. formula | AI-generated (Sprint 3) | After 2 weeks of data |
| Summary prompt strategy | One-shot vs. chain-of-thought | One-shot (speed) | Quality issues arise |
| Future AI provider | Claude API vs. OpenAI | Defer to Sprint 3+ | When Ollama quality insufficient |
| Semantic search | pgvector vs. Elasticsearch | pgvector (Sprint 6) | Scale exceeds single-node PG |
