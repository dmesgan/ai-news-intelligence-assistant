# AI News Intelligence Assistant

A self-hosted news aggregation and intelligence platform. Collects global news from GDELT, enriches each article with AI-generated summaries and categories using a local LLM (Ollama), ranks stories by relevance, and surfaces everything through a React dashboard with a daily digest.

No external AI APIs. No subscriptions. Full control.

---

## What It Does

- **Collects** news events from [GDELT 2.0](https://www.gdeltproject.org/) every 15 minutes
- **Summarizes** each article using a local Ollama LLM (Llama 3.2 by default)
- **Categorizes** articles into 8 topic domains (Politics, Tech, Economy, etc.)
- **Ranks** articles by a composite score: tone + recency + source diversity
- **Displays** everything in a React dashboard with category filtering
- **Generates** a daily morning digest of the top 10 stories

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Python 3.12, FastAPI, SQLAlchemy 2 (async) |
| Scheduling | APScheduler 3 |
| Database | PostgreSQL 16 |
| LLM | Ollama (Llama 3.2 3B default) |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, TanStack Query |
| Containers | Docker + docker-compose |
| Cloud (v2) | AWS ECS, RDS, S3, CloudFront, EventBridge |

---

## Quick Start

> Prerequisites: Docker, docker-compose, [Ollama](https://ollama.com) installed and running locally with `llama3.2:3b` pulled.

```bash
# 1. Clone and enter the project
git clone <repo-url>
cd ai-news-intelligence-assistant

# 2. Copy environment config
cp .env.example .env

# 3. Start everything
docker-compose up

# 4. Open the dashboard
open http://localhost:5173
```

The backend starts at `http://localhost:8000`. API docs at `http://localhost:8000/docs`.

---

## Project Structure

```
ai-news-intelligence-assistant/
├── backend/                  # Python FastAPI application
│   ├── app/
│   │   ├── api/              # FastAPI route handlers
│   │   ├── services/         # Collector, AI Processor, Digest Generator
│   │   ├── models/           # SQLAlchemy models
│   │   ├── schemas/          # Pydantic schemas
│   │   └── core/             # Config, scheduler, database
│   ├── migrations/           # Alembic migrations
│   └── Dockerfile
├── frontend/                 # React + TypeScript application
│   ├── src/
│   │   ├── pages/
│   │   ├── components/
│   │   ├── hooks/
│   │   └── api/
│   └── Dockerfile
├── docs/
│   ├── requirements/
│   │   └── project-vision.md       # Goals, requirements, user stories
│   ├── architecture/
│   │   └── system-overview.md      # Architecture, data model, tech decisions
│   └── sprints/
│       └── sprint-plan.md          # 5-sprint delivery plan
├── infra/                    # AWS infrastructure (Sprint 5)
├── docker-compose.yml
└── .env.example
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [Project Vision](docs/requirements/project-vision.md) | Goals, user stories, functional & non-functional requirements |
| [System Architecture](docs/architecture/system-overview.md) | Component design, data model, technology choices, AWS migration path |
| [Sprint Plan](docs/sprints/sprint-plan.md) | 5-sprint delivery roadmap with tasks and milestones |

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | `postgresql+asyncpg://...` | PostgreSQL connection string |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama API base URL |
| `OLLAMA_MODEL` | `llama3.2:3b` | Model name to use for summarization |
| `COLLECT_INTERVAL_MINUTES` | `15` | GDELT polling interval |
| `PROCESS_BATCH_SIZE` | `10` | Articles processed per AI cycle |
| `DIGEST_HOUR` | `7` | Hour (local time) to generate daily digest |
| `DIGEST_TOP_N` | `10` | Number of articles in daily digest |

---

## Development Status

| Sprint | Status | Deliverable |
|--------|--------|-------------|
| Sprint 1 — Foundation & Data Ingestion | Not started | GDELT → PostgreSQL |
| Sprint 2 — AI Summarization & Ranking | Not started | Ollama enrichment |
| Sprint 3 — REST API & Dashboard | Not started | React UI live |
| Sprint 4 — Digest & Polish | Not started | Production-local |
| Sprint 5 — AWS Deployment | Not started | Cloud deployment |

---

## License

MIT
