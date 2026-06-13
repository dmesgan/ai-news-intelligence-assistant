# AI News Intelligence Assistant

A self-hosted AI-powered news aggregation and intelligence platform. Collects global news from GDELT, enriches each article with AI-generated summaries, key points, importance scores, and categories using a local LLM (Ollama), and surfaces everything through a React dashboard with a daily digest.

No external AI APIs. No subscriptions. Full control.

---

## What It Does

- **Collects** news events from [GDELT 2.0](https://www.gdeltproject.org/) every 15 minutes
- **Summarizes** each article using a local Ollama LLM (Llama 3.1 by default)
- **Explains** key points and why each story matters
- **Categorizes** articles into 8 topic domains (AI, Tech, Economy, Cybersecurity, etc.)
- **Scores** articles by AI-assessed importance (1–100)
- **Displays** everything in a React dashboard with category filtering and search
- **Generates** a daily morning digest grouped by category
- **Searches** news history semantically via pgvector (Sprint 6)
- **Delivers** email digests and breaking news alerts via AWS SES (Sprint 7)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3, Maven |
| ORM | Spring Data JPA |
| Scheduling | Spring `@Scheduled` |
| Database | PostgreSQL 16 (+ pgvector in Sprint 6) |
| LLM | Ollama (Llama 3.1 default) |
| Future LLM | Claude API / OpenAI API |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, Axios |
| Containers | Docker + docker-compose |
| Cloud (v2) | AWS EC2, RDS, S3, CloudFront, SES, EventBridge |

---

## Quick Start

> Prerequisites: Docker, docker-compose, [Ollama](https://ollama.com) installed and running locally with `llama3.1` pulled.

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
├── backend/                          # Java 21 + Spring Boot 3 application
│   └── src/main/java/com/mesgan/ainews/
│       ├── controller/               # REST controllers
│       ├── service/                  # Business logic
│       ├── repository/               # Spring Data JPA repositories
│       ├── entity/                   # JPA entities
│       ├── dto/                      # Request / response DTOs
│       ├── mapper/                   # Entity ↔ DTO mappers
│       ├── client/                   # GDELT + Ollama HTTP clients
│       ├── scheduler/                # @Scheduled job classes
│       ├── config/                   # Spring configuration
│       └── exception/                # GlobalExceptionHandler + custom exceptions
├── frontend/                         # React + TypeScript application
│   └── src/
│       ├── pages/
│       ├── components/
│       └── api/
├── docs/
│   ├── requirements/
│   │   └── project-vision.md        # Goals, requirements, user stories
│   ├── architecture/
│   │   └── system-overview.md       # Architecture, data model, tech decisions
│   └── sprints/
│       └── sprint-plan.md           # 8-sprint delivery plan
├── infra/                            # AWS infrastructure (Sprint 8)
├── CLAUDE.md                         # AI coding assistant instructions
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
| `DB_URL` | `jdbc:postgresql://db:5432/ainews` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | `ainews` | PostgreSQL username |
| `DB_PASSWORD` | — | PostgreSQL password |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama API base URL |
| `OLLAMA_MODEL` | `llama3.1` | Model name to use for summarization |
| `GDELT_COLLECT_INTERVAL_MS` | `900000` | GDELT polling interval (ms) |
| `AI_PROCESS_BATCH_SIZE` | `10` | Articles processed per AI cycle |
| `DIGEST_CRON` | `0 0 7 * * *` | Cron expression for daily digest |
| `DIGEST_TOP_N` | `10` | Number of articles in daily digest |

---

## Development Status

| Sprint | Status | Deliverable |
|--------|--------|-------------|
| Sprint 1 — Backend Foundation | Not started | Spring Boot + PostgreSQL, all layers wired |
| Sprint 2 — News Ingestion | Not started | GDELT → PostgreSQL pipeline |
| Sprint 3 — AI Summaries | Not started | Ollama summaries + importance scores |
| Sprint 4 — Frontend Dashboard | Not started | React UI live |
| Sprint 5 — Daily Digest | Not started | Automated morning digest |
| Sprint 6 — Semantic Search | Not started | pgvector search + Q&A |
| Sprint 7 — Notifications | Not started | Email digest + breaking news alerts |
| Sprint 8 — AWS Migration | Not started | Full stack on AWS |

---

## License

MIT
