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
