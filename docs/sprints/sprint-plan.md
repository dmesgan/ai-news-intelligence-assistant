# Sprint Plan — AI News Intelligence Assistant

## Overview

| | |
|--|--|
| **Total duration** | 10 weeks (5 sprints × 2 weeks) |
| **Methodology** | Agile / Scrum-lite (solo developer) |
| **Definition of Done** | Feature works end-to-end, manually tested, committed |
| **Start date** | TBD |

Each sprint ends with a working vertical slice — something demonstrably running, not just code written.

---

## Sprint 1 — Foundation & Data Ingestion
**Duration:** Weeks 1–2  
**Theme:** Get real data flowing into a real database.

### Goals
- Project scaffolding and local dev environment
- GDELT collector working and storing articles
- Database schema live

### Tasks

#### Setup
- [ ] Initialize backend Python project (`pyproject.toml`, virtual env, `fastapi`, `sqlalchemy`, `apscheduler`, `httpx`)
- [ ] Initialize frontend project (`vite`, `react`, `typescript`, `tailwindcss`, `@tanstack/react-query`)
- [ ] Write `docker-compose.yml` with services: `db` (PostgreSQL), `backend`, `frontend`
- [ ] Create `.env.example` with all required environment variables
- [ ] Configure `alembic` for database migrations
- [ ] Write initial migration: `articles`, `digests`, `ingestion_log` tables

#### GDELT Collector
- [ ] Research GDELT 2.0 API endpoints (GKG vs. Events stream)
- [ ] Implement `GDELTCollector` class: fetch latest 15-min file, parse CSV
- [ ] Implement deduplication logic (URL-based, check before insert)
- [ ] Implement `ingestion_log` write on each run
- [ ] Register collector as APScheduler job (15-min interval)
- [ ] Write manual trigger endpoint `POST /api/admin/trigger-ingest`

#### Verification
- [ ] Run docker-compose, trigger ingestion, confirm > 10 articles in DB
- [ ] Confirm deduplication works (run twice, count stays the same)
- [ ] Confirm `ingestion_log` has a success record

### Sprint 1 Deliverable
Running `docker-compose up` results in articles being collected from GDELT and stored in PostgreSQL automatically every 15 minutes.

---

## Sprint 2 — AI Summarization & Categorization
**Duration:** Weeks 3–4  
**Theme:** Make every article intelligent.

### Goals
- Ollama integration working
- Every ingested article gets a summary and category
- Ranking score computed

### Prerequisites
- Ollama installed locally with `llama3.2:3b` pulled
- Sprint 1 complete

### Tasks

#### Ollama Integration
- [ ] Implement `OllamaClient` wrapper around `POST /api/generate`
- [ ] Design and test summarization + categorization prompt (single call, JSON output)
- [ ] Implement JSON response parsing with fallback for malformed output
- [ ] Add retry logic (max 3 attempts) with exponential backoff
- [ ] Add configurable model name via environment variable

#### AI Processor
- [ ] Implement `AIProcessor` class: SELECT pending → call Ollama → UPDATE article
- [ ] Process articles in batches (configurable size, default 10 per cycle)
- [ ] Update `status` to `processed` or `failed` with error message stored
- [ ] Register processor as APScheduler job (2-minute interval)

#### Ranking Engine
- [ ] Implement `RankingEngine.compute_score(article)` with formula from architecture doc
- [ ] Call ranking after each article is processed
- [ ] Implement bulk re-rank endpoint `POST /api/admin/rerank` for tuning

#### Verification
- [ ] Ingest 50+ articles, confirm all get summaries within 10 minutes
- [ ] Confirm category values are all within the defined taxonomy
- [ ] Confirm `rank_score` is populated and non-uniform across articles
- [ ] Test failure path: kill Ollama mid-run, confirm `status='failed'` and retry works

### Sprint 2 Deliverable
Every ingested article automatically receives an AI-generated summary, a category, and a rank score. The system recovers gracefully from Ollama unavailability.

---

## Sprint 3 — REST API & React Dashboard
**Duration:** Weeks 5–6  
**Theme:** Make data visible and useful.

### Goals
- All API endpoints implemented and documented
- React dashboard showing ranked, filterable news

### Tasks

#### FastAPI Endpoints
- [ ] `GET /api/articles` — paginated, filter by category and date range, sorted by rank_score
- [ ] `GET /api/articles/{id}` — full article detail
- [ ] `GET /api/categories` — categories with article counts
- [ ] `GET /api/status` — last ingestion time, pending count, processed count, failed count
- [ ] `GET /api/digest/latest` — latest daily digest
- [ ] `GET /api/digest/{date}` — digest by date
- [ ] Enable CORS for local frontend dev

#### React Dashboard
- [ ] Project structure: `pages/`, `components/`, `hooks/`, `api/`
- [ ] API client layer (`api/client.ts`) using TanStack Query
- [ ] `ArticleCard` component: title, source, category badge, summary, score, timestamp
- [ ] `ArticleList` page: ranked cards with infinite scroll or pagination
- [ ] `CategoryFilter` component: filter pills for each category
- [ ] `DateRangePicker` component: filter by published date
- [ ] `StatusBar` component: last ingestion time, article counts
- [ ] `DigestPage`: display latest digest with date navigation
- [ ] Auto-refresh every 5 minutes via TanStack Query `refetchInterval`

#### Verification
- [ ] Open dashboard, confirm articles are visible and ranked
- [ ] Filter by each category, confirm results update correctly
- [ ] Confirm status bar shows accurate counts
- [ ] Confirm auto-refresh updates content without page reload

### Sprint 3 Deliverable
A working React dashboard at `localhost:5173` showing ranked, summarized, categorized articles with live refresh.

---

## Sprint 4 — Daily Digest & Polish
**Duration:** Weeks 7–8  
**Theme:** Production-quality local deployment.

### Goals
- Daily digest generated automatically each morning
- Error handling, logging, and observability hardened
- UX polished

### Tasks

#### Daily Digest
- [ ] Implement `DigestGenerator`: SELECT top-10 articles from past 24h → prompt Ollama for narrative digest → INSERT into `digests`
- [ ] Craft digest prompt (cohesive briefing style, not just concatenation)
- [ ] Register as APScheduler cron job at 07:00 local
- [ ] Add manual trigger `POST /api/admin/generate-digest`
- [ ] Display digest on dashboard DigestPage with article links

#### Hardening
- [ ] Structured logging (`structlog`) for all services: collector, processor, API
- [ ] Health check endpoint `GET /api/health` for docker-compose `healthcheck`
- [ ] Add `ARTICLES_PER_PAGE`, `DIGEST_TOP_N`, `OLLAMA_MODEL`, `COLLECT_INTERVAL_MINUTES` to env config
- [ ] Handle GDELT API downtime gracefully (log + skip cycle, no crash)
- [ ] Add `failed` article retry queue (re-process `status='failed'` articles older than 1h)

#### UX Polish
- [ ] Loading skeleton states on ArticleList
- [ ] Empty state when no articles in category
- [ ] Error boundary and user-facing error messages
- [ ] Dark/light mode toggle
- [ ] Article source link (opens original article in new tab)

#### Verification
- [ ] Manually trigger digest, confirm it appears in dashboard
- [ ] Simulate 07:00 trigger, confirm digest stored with correct date
- [ ] Crash Ollama service, confirm system logs gracefully and resumes
- [ ] Load test: 500 articles in DB, confirm dashboard loads in < 2s

### Sprint 4 Deliverable
A fully functional, polished local application with daily digests, robust error handling, and a smooth user experience.

---

## Sprint 5 — AWS Deployment
**Duration:** Weeks 9–10  
**Theme:** Production on AWS.

### Goals
- Complete stack running on AWS
- Environment parity with local
- Basic cost monitoring

### Tasks

#### Infrastructure
- [ ] Create `infra/` directory with Terraform or CloudFormation templates
- [ ] Provision RDS PostgreSQL instance (db.t3.micro for start)
- [ ] Provision ECS Fargate cluster and task definitions for `backend` service
- [ ] Provision S3 bucket + CloudFront distribution for React SPA
- [ ] Provision EventBridge Scheduler rules to replace APScheduler jobs
- [ ] Set up Secrets Manager for database credentials and config

#### Application Changes for AWS
- [ ] Replace APScheduler jobs with ECS Task invocations (or keep APScheduler in ECS task — decision point)
- [ ] Update database connection to use RDS endpoint from environment
- [ ] Build and push Docker images to ECR
- [ ] Configure CloudFront to proxy `/api/*` to ALB (or use API Gateway)
- [ ] Decide on Ollama strategy: EC2 GPU instance vs. Bedrock migration

#### Observability
- [ ] Ship logs to CloudWatch Logs
- [ ] Create CloudWatch Dashboard: articles/day, digest generation success rate, LLM latency
- [ ] Set billing alert at $20/month threshold

#### Verification
- [ ] GDELT collection running on AWS schedule
- [ ] Articles summarized and ranked in RDS
- [ ] Dashboard accessible via CloudFront URL
- [ ] Daily digest generated on schedule
- [ ] Confirm cost < $30/month at steady state

### Sprint 5 Deliverable
Complete stack running on AWS, accessible via CloudFront, with monitoring and cost controls.

---

## Milestone Summary

| Milestone | Sprint | Deliverable |
|-----------|--------|-------------|
| M1: Data flows | Sprint 1 | GDELT → PostgreSQL pipeline running |
| M2: AI enriched | Sprint 2 | Summaries + categories + ranking live |
| M3: UI live | Sprint 3 | React dashboard fully functional |
| M4: Production-local | Sprint 4 | Daily digest + hardened local app |
| M5: Cloud deployed | Sprint 5 | Full stack on AWS |

---

## Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Ollama too slow on CPU for article volume | Medium | High | Reduce batch size; upgrade to 8B model when GPU available |
| GDELT API structure changes | Low | High | Pin to specific GDELT endpoint version; add schema validation |
| LLM produces invalid JSON for category | Medium | Medium | Add regex fallback + `failed` status with retry |
| AWS costs exceed estimate | Low | Medium | Set billing alerts; use RDS t3.micro + Fargate spot |
| GDELT content too noisy for useful ranking | Medium | Medium | Add configurable country/theme filters to collector |
