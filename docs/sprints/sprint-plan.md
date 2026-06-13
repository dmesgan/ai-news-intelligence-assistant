# Sprint Plan — AI News Intelligence Assistant

## Overview

| | |
|--|--|
| **Total duration** | 16 weeks (8 sprints × 2 weeks) |
| **Methodology** | Agile / Scrum-lite (solo developer) |
| **Definition of Done** | Feature works end-to-end, manually tested, committed |
| **Start date** | TBD |

Each sprint ends with a working vertical slice — something demonstrably running, not just code written.

---

## Sprint 1 — Backend Foundation
**Duration:** Weeks 1–2
**Theme:** Solid Java foundation before any external integrations.

### Goals
- Spring Boot project running with PostgreSQL
- All entities, repositories, services, controllers, and DTOs in place
- Global exception handler wired up
- No AI, no GDELT, no frontend, no scheduler

### Tasks

#### Project Setup
- [ ] Create Spring Boot 3 project via Spring Initializr (Java 21, Maven)
- [ ] Dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, Lombok
- [ ] Write `docker-compose.yml` with `db` (PostgreSQL 16) and `backend` services
- [ ] Create `application.yml` with datasource, JPA, and server config
- [ ] Create `.env.example` with all required environment variables

#### Database
- [ ] Configure Hibernate DDL auto (`validate`) and write manual SQL schema
- [ ] Create `articles` table (UUID PK, all fields per CLAUDE.md)
- [ ] Create `summaries` table (UUID PK, UUID FK to articles)
- [ ] Create `daily_digests` table (UUID PK, digest_date unique)
- [ ] Add indexes: `articles(processed)`, `articles(importance_score DESC)`, `articles(category)`, `summaries(article_id)`

#### Entities
- [ ] `Article` entity — all fields from CLAUDE.md, `@Table("articles")`
- [ ] `Summary` entity — all fields from CLAUDE.md, `@OneToOne` with Article
- [ ] `DailyDigest` entity — all fields from CLAUDE.md

#### Repositories
- [ ] `ArticleRepository` — `findByProcessedFalse()`, `findByUrl()`, `findByCategory()`
- [ ] `SummaryRepository` — `findByArticleId()`
- [ ] `DailyDigestRepository` — `findByDigestDate()`

#### DTOs & Mappers
- [ ] `ArticleResponseDto` — fields for dashboard display
- [ ] `ArticleSummaryResponseDto` — article + embedded summary for detail view
- [ ] `DigestResponseDto`
- [ ] `ArticleMapper` — entity to DTO (manual or MapStruct)
- [ ] `DigestMapper`

#### Services
- [ ] `NewsService` — `getLatestArticles(Pageable)`, `searchArticles(String keyword, Pageable)`, `getArticleById(UUID)`
- [ ] `DigestService` — `getTodayDigest()`, `getDigestByDate(LocalDate)`

#### Controllers
- [ ] `NewsController` — `GET /api/news/latest`, `GET /api/news/search`, `GET /api/news/{id}`
- [ ] `DigestController` — `GET /api/digest/today`, `GET /api/digest/{date}`
- [ ] `StatusController` — `GET /api/status`, `GET /api/health`
- [ ] Configure CORS for localhost frontend

#### Exception Handling
- [ ] `GlobalExceptionHandler` (`@ControllerAdvice`)
- [ ] `ResourceNotFoundException` (→ 404)
- [ ] `ValidationException` (→ 400)
- [ ] Consistent error response DTO

#### Verification
- [ ] `docker-compose up` starts backend + PostgreSQL without errors
- [ ] `GET /api/health` returns 200
- [ ] Manually insert an article row in DB, confirm `GET /api/news/latest` returns it as DTO
- [ ] Call `GET /api/news/{invalid-id}`, confirm 404 JSON error response

### Sprint 1 Deliverable
A running Spring Boot application with PostgreSQL, all layers wired (controller → service → repository), proper DTOs, and global exception handling. No external integrations yet.

---

## Sprint 2 — News Ingestion
**Duration:** Weeks 3–4
**Theme:** Get real data flowing from GDELT into the database automatically.

### Prerequisites
- Sprint 1 complete

### Goals
- GDELT collector running on schedule
- Articles deduplicated and persisted
- Manual trigger endpoint for testing

### Tasks

#### GDELT Client
- [ ] Research GDELT 2.0 API — identify correct endpoint for latest 15-min update file
- [ ] Implement `GdeltClient` using Spring `WebClient`
- [ ] Parse GDELT CSV response, map to `Article` objects
- [ ] Handle HTTP errors and timeouts gracefully (log + skip cycle)

#### Ingestion Service
- [ ] Implement `NewsIngestionService.ingestLatest()` — fetch → deduplicate by URL → batch insert
- [ ] Deduplication: `ArticleRepository.existsByUrl(url)` check before insert
- [ ] Log ingestion results: articles found, articles new, duration

#### Scheduler
- [ ] Implement `GdeltCollectorScheduler` with `@Scheduled(fixedDelayString = "${gdelt.collect-interval-ms}")`
- [ ] Add manual trigger: `POST /api/admin/trigger-ingest` (calls same service method)

#### Verification
- [ ] Trigger ingestion manually, confirm > 10 articles inserted
- [ ] Run ingestion twice, confirm duplicate articles are skipped (count stays same)
- [ ] Check logs for ingestion summary (found / new / duration)
- [ ] Kill network mid-run, confirm graceful log + no crash

### Sprint 2 Deliverable
Articles collected from GDELT every 15 minutes and stored in PostgreSQL automatically. `GET /api/news/latest` returns real news.

---

## Sprint 3 — AI Summaries
**Duration:** Weeks 5–6
**Theme:** Make every article intelligent with Ollama.

### Prerequisites
- Ollama installed locally with `llama3.1` pulled (`ollama pull llama3.1`)
- Sprint 2 complete

### Goals
- Every ingested article gets an AI-generated summary, category, and importance score
- Results stored in `summaries` table
- System recovers gracefully if Ollama is unavailable

### Tasks

#### Ollama Client
- [ ] Implement `OllamaClient` using `WebClient` — `POST /api/generate` with JSON body
- [ ] Configure `OLLAMA_BASE_URL` and `OLLAMA_MODEL` from environment
- [ ] Implement retry logic (max 3 attempts, exponential backoff) using Spring Retry

#### Prompt Design
- [ ] Design single prompt that returns JSON with all required fields:
  ```json
  {
    "oneSentenceSummary": "...",
    "keyPoints": "...",
    "whyItMatters": "...",
    "aiCategory": "...",
    "importanceScore": 75
  }
  ```
- [ ] Implement Jackson parsing of Ollama response with fallback for malformed JSON
- [ ] Validate `aiCategory` is within defined taxonomy; default to `"General"` if not

#### AI Processor
- [ ] Implement `SummaryService.processUnprocessedArticles(int batchSize)`
- [ ] Fetch batch of `processed=false` articles → call Ollama → save `Summary` → update `Article.processed=true` and `Article.importanceScore`
- [ ] On Ollama failure: log error, leave `processed=false` for next cycle (automatic retry)
- [ ] Implement `AiProcessorScheduler` with `@Scheduled(fixedDelay = 120_000)`

#### API Updates
- [ ] Update `GET /api/news/latest` to JOIN summaries and include `oneSentenceSummary` in response
- [ ] Update `GET /api/news/{id}` to include full summary detail (keyPoints, whyItMatters)
- [ ] Update `GET /api/status` to include `unprocessed article count`

#### Verification
- [ ] Ingest 20+ articles, confirm all summaries generated within 10 minutes
- [ ] Confirm all `aiCategory` values are within the taxonomy
- [ ] Confirm `importanceScore` varies across articles (not all same value)
- [ ] Kill Ollama, confirm system logs gracefully and resumes when Ollama restarts
- [ ] Confirm `GET /api/news/{id}` returns full summary detail

### Sprint 3 Deliverable
Every ingested article automatically receives a one-sentence summary, key points, why-it-matters explanation, AI category, and importance score. The system is resilient to Ollama downtime.

---

## Sprint 4 — Frontend Dashboard
**Duration:** Weeks 7–8
**Theme:** Make data visible and useful through a React UI.

### Goals
- React dashboard displaying ranked, summarized news
- Category filtering and keyword search
- Article detail view
- System status display

### Tasks

#### Project Setup
- [ ] Initialize frontend: `npm create vite@latest frontend -- --template react-ts`
- [ ] Install: `tailwindcss`, `axios`, `react-router-dom`
- [ ] Configure Tailwind, set up API base URL from env
- [ ] Add `frontend` service to `docker-compose.yml`

#### API Client Layer
- [ ] `api/client.ts` — Axios instance with base URL and error interceptor
- [ ] `api/news.ts` — `getLatestNews()`, `searchNews()`, `getNewsById()`
- [ ] `api/digest.ts` — `getTodayDigest()`, `getDigestByDate()`
- [ ] `api/status.ts` — `getStatus()`

#### Components
- [ ] `ArticleCard` — title, source, category badge, one-sentence summary, importance score, published date
- [ ] `ArticleDetail` — full article view with key points and why-it-matters
- [ ] `CategoryFilter` — filter pills for each category
- [ ] `SearchBar` — keyword search input with debounce
- [ ] `StatusBar` — last ingestion time, article counts
- [ ] `DigestView` — today's digest with date navigation
- [ ] Loading skeleton states on all list views
- [ ] Empty state components for no-results

#### Pages
- [ ] `NewsFeedPage` — ranked article list with category filter + search
- [ ] `ArticleDetailPage` — full detail routed by `/news/:id`
- [ ] `DigestPage` — daily digest at `/digest`

#### Verification
- [ ] Open `localhost:5173`, confirm articles load ranked by importance score
- [ ] Filter by each category, confirm results update
- [ ] Search for a keyword, confirm matching articles appear
- [ ] Click an article, confirm detail page shows summary, key points, and why-it-matters
- [ ] Navigate to digest page, confirm today's digest displays

### Sprint 4 Deliverable
A fully functional React dashboard at `localhost:5173` showing ranked, AI-summarized news with category filtering, search, and article detail views.

---

## Sprint 5 — Daily Digest
**Duration:** Weeks 9–10
**Theme:** Automated daily intelligence reports.

### Goals
- Digest generated automatically every morning
- Covers top stories per category
- Viewable in dashboard and stored in DB

### Tasks

#### Digest Generation
- [ ] Implement `DigestService.generateDailyDigest()` — select top-N articles per category from past 24h, call Ollama to write cohesive narrative, store in `daily_digests`
- [ ] Craft digest prompt: briefing style, grouped by category, concise
- [ ] Implement `DigestScheduler` with `@Scheduled(cron = "0 0 7 * * *")`
- [ ] Add manual trigger: `POST /api/admin/generate-digest`
- [ ] Guard: skip if digest for today already exists

#### Dashboard Integration
- [ ] Update `DigestPage` to show today's digest with category sections
- [ ] Add date navigation to view past digests
- [ ] Link digest article references back to article detail pages

#### Hardening
- [ ] Handle case: no articles in past 24h → log warning, skip digest
- [ ] Handle case: Ollama unavailable at 07:00 → retry once at 07:30, log failure

#### Verification
- [ ] Trigger digest manually, confirm it appears in dashboard
- [ ] Confirm `GET /api/digest/today` returns digest content
- [ ] Confirm `GET /api/digest/{yesterday}` returns previous digest
- [ ] Confirm digest covers multiple categories

### Sprint 5 Deliverable
A daily digest generated every morning at 07:00, grouped by category, stored in PostgreSQL, and viewable in the dashboard.

---

## Sprint 6 — Semantic Search
**Duration:** Weeks 11–12
**Theme:** Search and explore news history with natural language.

### Goals
- Vector embeddings stored for all article summaries
- Semantic similarity search across articles
- Natural language Q&A over news history

### Tasks
- [ ] Enable `pgvector` PostgreSQL extension
- [ ] Add `article_embeddings` table with `vector(768)` column
- [ ] Implement embedding generation via Ollama embedding model or local model
- [ ] Backfill embeddings for existing articles
- [ ] Implement `ArticleRepository.findSimilar(float[] embedding, int limit)`
- [ ] `GET /api/news/similar/{id}` — articles similar to a given article
- [ ] `GET /api/news/ask?q=...` — natural language Q&A (retrieve top-K relevant articles → summarize with Ollama)
- [ ] Add semantic search input to dashboard

### Sprint 6 Deliverable
Users can find semantically similar articles and ask natural language questions answered using historical news context.

---

## Sprint 7 — Notifications
**Duration:** Weeks 13–14
**Theme:** Proactive intelligence delivery.

### Goals
- Daily email digest delivered to configured address
- Breaking news alerts by category

### Tasks
- [ ] Integrate AWS SES (or JavaMail for local dev with MailHog)
- [ ] Implement `EmailService` — format and send digest email
- [ ] Trigger email after `DigestService.generateDailyDigest()` completes
- [ ] Implement breaking news detection: article with `importanceScore >= threshold` triggers alert email
- [ ] Configure recipient address, thresholds, and categories via environment variables
- [ ] Add email send status to `GET /api/status`

### Sprint 7 Deliverable
Daily digest emails delivered every morning. High-importance breaking news triggers an alert email to the configured address.

---

## Sprint 8 — AWS Migration
**Duration:** Weeks 15–16
**Theme:** Production on AWS.

### Goals
- Complete stack running on AWS
- Environment parity with local
- Cost monitoring in place

### Tasks

#### Infrastructure
- [ ] Create `infra/` with Terraform or CloudFormation templates
- [ ] Provision RDS PostgreSQL (db.t3.micro) with pgvector extension enabled
- [ ] Provision ECS Fargate cluster + task definition for Spring Boot backend
- [ ] Provision S3 bucket + CloudFront distribution for React SPA
- [ ] Provision EventBridge Scheduler rules (replace `@Scheduled` or keep in ECS)
- [ ] Provision SES sending identity + verified recipient
- [ ] Configure Secrets Manager for DB credentials, Ollama URL, SES config

#### Application Changes
- [ ] Update `application.yml` to read all config from environment
- [ ] Build and push Docker images to ECR
- [ ] Configure CloudFront to proxy `/api/*` to ALB
- [ ] Decide Ollama strategy: EC2 GPU instance vs. Claude API / Bedrock

#### Observability
- [ ] Ship Spring Boot logs to CloudWatch Logs
- [ ] Create CloudWatch Dashboard: articles/day, summaries/day, digest success rate
- [ ] Set billing alert at $25/month threshold

#### Verification
- [ ] GDELT ingestion running on AWS schedule
- [ ] Articles summarized and stored in RDS
- [ ] Dashboard accessible via CloudFront URL
- [ ] Daily digest generated, stored, and emailed on schedule
- [ ] Confirm cost < $35/month at steady state

### Sprint 8 Deliverable
Complete stack deployed on AWS: EC2/ECS for backend, RDS for PostgreSQL, S3/CloudFront for frontend, SES for email, with cost monitoring.

---

## Milestone Summary

| Milestone | Sprint | Deliverable |
|-----------|--------|-------------|
| M1: Backend foundation | Sprint 1 | Spring Boot + PostgreSQL, all layers wired |
| M2: Data flows | Sprint 2 | GDELT → PostgreSQL pipeline running |
| M3: AI enriched | Sprint 3 | Ollama summaries + importance scores live |
| M4: UI live | Sprint 4 | React dashboard fully functional |
| M5: Daily digest | Sprint 5 | Automated morning digest |
| M6: Semantic search | Sprint 6 | pgvector search + Q&A live |
| M7: Notifications | Sprint 7 | Email digest + breaking news alerts |
| M8: Cloud deployed | Sprint 8 | Full stack on AWS |

---

## Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Ollama too slow on CPU for article volume | Medium | High | Reduce batch size; upgrade model when GPU available |
| GDELT API structure changes | Low | High | Pin to specific endpoint version; add schema validation on parse |
| LLM returns malformed JSON | Medium | Medium | Jackson fallback parser; mark article `processed=false` for retry |
| Importance scores not meaningful | Medium | Medium | Review sample of scored articles after Sprint 3; tune prompt |
| AWS costs exceed estimate | Low | Medium | Billing alerts; use t3.micro instances; Fargate spot |
| pgvector performance at scale | Low | Medium | Evaluate HNSW index; fallback to keyword search if needed |
