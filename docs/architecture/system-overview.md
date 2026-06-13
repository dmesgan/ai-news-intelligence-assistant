# System Architecture — AI News Intelligence Assistant

## 1. Architecture Style

**Layered Monolith with async workers (v1) → Microservices on AWS (v2)**

In v1, all backend components run in a single Python process space using an async task scheduler. This keeps local deployment simple (one `docker-compose up`) while keeping the internal boundaries clean enough to extract into separate services for AWS deployment.

---

## 2. High-Level Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        USER'S MACHINE (v1)                   │
│                                                              │
│  ┌──────────────┐     ┌──────────────────────────────────┐  │
│  │  React UI    │────▶│         FastAPI Backend           │  │
│  │  (Vite/TS)   │◀────│  /api/articles  /api/digest       │  │
│  └──────────────┘     └────────┬─────────────────────────┘  │
│                                │                             │
│              ┌─────────────────┼─────────────────┐          │
│              │                 │                 │           │
│              ▼                 ▼                 ▼           │
│  ┌──────────────────┐ ┌──────────────┐ ┌──────────────────┐ │
│  │  GDELT Collector │ │  AI Processor│ │  Digest Generator│ │
│  │  (APScheduler)   │ │  (APScheduler│ │  (APScheduler)   │ │
│  └────────┬─────────┘ └──────┬───────┘ └────────┬─────────┘ │
│           │                  │                  │            │
│           ▼                  ▼                  │            │
│  ┌──────────────────────────────────────────┐   │           │
│  │           PostgreSQL Database             │◀──┘           │
│  │  articles | summaries | digests | logs   │               │
│  └──────────────────────────────────────────┘               │
│                             │                               │
│                             ▼                               │
│                   ┌──────────────────┐                      │
│                   │   Ollama (local) │                      │
│                   │   llama3 / mistral│                     │
│                   └──────────────────┘                      │
└─────────────────────────────────────────────────────────────┘
         │ GDELT 2.0 HTTP endpoint (external, read-only)
         ▼
    api.gdeltproject.org
```

---

## 3. Component Breakdown

### 3.1 GDELT Collector

- **Responsibility:** Polls the GDELT 2.0 API every 15 minutes, parses event CSV/JSON, deduplicates by URL, and writes raw records to `articles` table with status `pending`.
- **Technology:** Python, `httpx` for async HTTP, `pandas` for CSV parsing.
- **Trigger:** APScheduler cron job (interval-based, configurable).
- **Output:** Rows in `articles` with `status = 'pending'`.

**Why GDELT 2.0:** It updates every 15 minutes, is free, machine-readable, and includes tone scores, geographic tags, and source metadata — exactly the signals needed for ranking.

---

### 3.2 AI Processor

- **Responsibility:** Picks up `pending` articles, calls Ollama for summarization and categorization, writes results back, updates status to `processed`.
- **Technology:** Python, `httpx` to call Ollama REST API (`POST /api/generate`).
- **Trigger:** APScheduler job running every 2 minutes, processes up to N articles per cycle (configurable, default: 10).
- **Concurrency:** Sequential within a cycle to avoid overwhelming Ollama on CPU-bound hardware. Can be made concurrent once GPU is available.
- **Prompt pattern:** Single prompt requests both summary and category JSON in one call to minimize LLM round-trips.

**Why Ollama:** Runs entirely offline, supports swappable models (Llama 3.2, Mistral, Gemma), and exposes a simple REST API identical in shape to what we'd call on AWS Bedrock — making future migration straightforward.

---

### 3.3 Ranking Engine

- **Responsibility:** Computes a composite `rank_score` for each article.
- **Location:** Runs as part of the AI Processor step (post-summarization) and is re-run on each ingestion cycle for recency decay.
- **Scoring formula (v1):**

```
rank_score = (0.4 × normalized_gdelt_tone)
           + (0.4 × recency_decay)
           + (0.2 × source_diversity_bonus)

recency_decay = e^(-λ × hours_since_published)   # λ = 0.1
```

**Why this formula:** Tone and recency are the two signals most correlated with "importance" in GDELT research. Source diversity prevents one outlet from dominating the feed.

---

### 3.4 Digest Generator

- **Responsibility:** Once per day, selects the top N scored articles from the past 24 hours, calls Ollama to write a cohesive narrative digest, stores it in the `digests` table.
- **Trigger:** APScheduler cron job at 07:00 local time (configurable).
- **Technology:** Same Python/Ollama stack as AI Processor.

---

### 3.5 FastAPI Backend

- **Responsibility:** Exposes REST endpoints consumed by the React dashboard.
- **Technology:** Python, FastAPI, SQLAlchemy (async), Pydantic v2 for schemas.
- **Key endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/articles` | Paginated, filterable article list |
| GET | `/api/articles/{id}` | Single article with full summary |
| GET | `/api/digest/latest` | Most recent daily digest |
| GET | `/api/digest/{date}` | Digest for a specific date |
| GET | `/api/status` | Ingestion health + last run timestamps |
| GET | `/api/categories` | Category list with article counts |

---

### 3.6 React Dashboard

- **Responsibility:** Displays ranked news, supports category filtering, shows daily digest, displays system status.
- **Technology:** React 18, TypeScript, Vite, TanStack Query (data fetching/caching), Tailwind CSS.
- **Architecture:** SPA served from `localhost:5173` in dev, built static assets served by FastAPI or Nginx in production.

**Why TanStack Query:** Handles polling (`refetchInterval`) for live updates and caching cleanly — avoids hand-rolling fetch logic.

---

## 4. Data Model

```sql
-- Core article store
CREATE TABLE articles (
    id              SERIAL PRIMARY KEY,
    url             TEXT UNIQUE NOT NULL,
    title           TEXT,
    source          TEXT,
    published_at    TIMESTAMPTZ,
    raw_content     TEXT,
    gdelt_tone      FLOAT,
    gdelt_themes    TEXT[],
    gdelt_geo       TEXT,
    summary         TEXT,
    category        TEXT,
    rank_score      FLOAT DEFAULT 0,
    status          TEXT DEFAULT 'pending',  -- pending | processed | failed
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    processed_at    TIMESTAMPTZ
);

CREATE INDEX idx_articles_status     ON articles(status);
CREATE INDEX idx_articles_rank_score ON articles(rank_score DESC);
CREATE INDEX idx_articles_published  ON articles(published_at DESC);
CREATE INDEX idx_articles_category   ON articles(category);

-- Daily digest store
CREATE TABLE digests (
    id          SERIAL PRIMARY KEY,
    date        DATE UNIQUE NOT NULL,
    content     TEXT NOT NULL,
    article_ids INTEGER[],
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Ingestion run log
CREATE TABLE ingestion_log (
    id              SERIAL PRIMARY KEY,
    run_at          TIMESTAMPTZ DEFAULT NOW(),
    articles_found  INTEGER,
    articles_new    INTEGER,
    duration_ms     INTEGER,
    status          TEXT,   -- success | partial | failed
    error_message   TEXT
);
```

---

## 5. Technology Stack

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Language (backend) | Python 3.12 | Best ecosystem for AI/data work; GDELT libs available |
| Web framework | FastAPI | Async-native, auto OpenAPI docs, Pydantic integration |
| ORM | SQLAlchemy 2 (async) | Type-safe, async-first, production-proven |
| Task scheduling | APScheduler 3 | Lightweight in-process scheduler; no Redis/Celery needed in v1 |
| Database | PostgreSQL 16 | Reliable, array column support for tags, future AWS RDS compatible |
| LLM runtime | Ollama | Local, model-agnostic, REST API matches cloud LLM patterns |
| Default LLM model | Llama 3.2 (3B) | Fast on CPU, good summarization quality at 3B params |
| Frontend framework | React 18 + TypeScript | Industry standard; strong ecosystem |
| Build tool | Vite | Fast HMR, simple config |
| UI/Styling | Tailwind CSS | Utility-first, no design system overhead in v1 |
| Data fetching | TanStack Query | Caching + polling built-in |
| Containerization | Docker + docker-compose | Single-command local run; maps directly to ECS in v2 |

---

## 6. Data Flow

```
[GDELT API] ──HTTP poll──▶ [Collector] ──INSERT──▶ [articles: status=pending]
                                                              │
                              [AI Processor] ──SELECT pending─┘
                                    │
                                    ├──POST /api/generate──▶ [Ollama]
                                    │                            │
                                    │◀───── {summary, category} ─┘
                                    │
                                    └──UPDATE articles SET summary, category,
                                                rank_score, status='processed'

[React UI] ──GET /api/articles──▶ [FastAPI] ──SELECT processed──▶ [PostgreSQL]
                                       │◀──────── ranked results ──────────────┘
                                       │
                                  [JSON response] ──▶ [React renders cards]

[07:00 cron] ──▶ [Digest Generator] ──SELECT top-N──▶ [PostgreSQL]
                         │──POST /api/generate──▶ [Ollama]
                         └──INSERT INTO digests──▶ [PostgreSQL]
```

---

## 7. AWS Migration Path (v2)

| v1 Component | AWS Equivalent |
|-------------|----------------|
| PostgreSQL (local) | Amazon RDS (PostgreSQL) |
| Ollama (local) | Amazon Bedrock (Claude Haiku / Titan) or EC2 GPU instance with Ollama |
| APScheduler (in-process) | Amazon EventBridge Scheduler + ECS Tasks |
| FastAPI (local) | ECS Fargate (behind ALB) |
| React SPA (local) | S3 + CloudFront |
| docker-compose | ECS Task Definitions |
| Environment variables | AWS Systems Manager Parameter Store / Secrets Manager |

The internal service boundaries defined in v1 map directly to ECS services in v2. No code rewrites required — only infrastructure changes.

---

## 8. Security Considerations (v1)

- Dashboard runs on localhost only; no public exposure in v1.
- No credentials stored in code; all via `.env` file (gitignored).
- Ollama is bound to localhost only.
- PostgreSQL accessible only within the Docker network.
- GDELT is a public read-only API; no API key required.

---

## 9. Open Architecture Decisions

| Decision | Options | Current Choice | Revisit When |
|----------|---------|---------------|--------------|
| LLM model | Llama 3.2 3B / 8B, Mistral 7B | Llama 3.2 3B (speed) | GPU available → 8B |
| Summary prompt strategy | One-shot vs. chain-of-thought | One-shot (speed) | Quality issues arise |
| Ranking weights | Adjust formula coefficients | 40/40/20 | After 2 weeks of data |
| Async processing | Sequential vs. concurrent | Sequential | GPU acceleration |
