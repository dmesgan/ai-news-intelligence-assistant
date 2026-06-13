# Project Vision — AI News Intelligence Assistant

## 1. Overview

The AI News Intelligence Assistant is a self-hosted AI-powered news aggregation and intelligence platform. It ingests global news events from GDELT, enriches each article with AI-generated summaries and importance scores using a local LLM (Ollama), and surfaces ranked, categorized news through a React dashboard. A daily digest is generated automatically so users never miss what matters — without information overload.

---

## 2. Problem Statement

Most people consume news from multiple websites and social media platforms. This creates information overload, repetition, and a lack of context about why a story matters.

This application solves that problem by:

1. Aggregating news from multiple trusted sources.
2. Removing information overload.
3. Generating concise AI summaries.
4. Explaining why a story matters.
5. Ranking stories by importance.
6. Providing daily intelligence reports.
7. Allowing semantic search across historical news.

The application should feel like having a personal AI news analyst.

---

## 3. Goals

| # | Goal | Priority |
|---|------|----------|
| G1 | Collect structured news events from GDELT continuously | Must Have |
| G2 | Persist articles and metadata in PostgreSQL | Must Have |
| G3 | Summarize each article using a local Ollama LLM | Must Have |
| G4 | Categorize articles and score importance via AI | Must Have |
| G5 | Display ranked, summarized news in a React dashboard | Must Have |
| G6 | Generate and store a daily digest | Must Have |
| G7 | Enable semantic search across article history | Future |
| G8 | Send email digests and breaking news alerts | Future |
| G9 | Deploy the complete stack to AWS | Future |

---

## 4. Non-Goals (v1)

- Multi-user authentication and role-based access
- Mobile native app
- News sources beyond GDELT (RSS, Twitter/X, Webhooks)
- Fine-tuning or training custom models
- Paid external AI APIs (Claude API, OpenAI) — future only

---

## 5. Users

**Primary User:** A solo analyst or developer who wants a self-hosted, AI-assisted news briefing every day without vendor lock-in.

**Use Cases:**

| ID | As a user, I want to… | So that… |
|----|----------------------|----------|
| UC1 | See today's top news ranked by importance | I can quickly scan what matters |
| UC2 | Read a one-sentence AI summary per article | I don't have to read full articles |
| UC3 | Understand key points and why a story matters | I get instant context |
| UC4 | Filter news by category (AI, Tech, Economy…) | I can focus on specific domains |
| UC5 | Receive a daily digest of top stories | I stay informed without active monitoring |
| UC6 | Search news history semantically | I can find related stories across time |
| UC7 | Receive an email when breaking news hits | I'm alerted to critical events |

---

## 6. Functional Requirements

### 6.1 Data Ingestion (Sprint 2)
- FR-01: The system MUST poll GDELT 2.0 on a configurable schedule (default: every 15 minutes).
- FR-02: The system MUST deduplicate articles by URL before storage.
- FR-03: The system MUST store GDELT event metadata alongside article content.
- FR-04: Failed ingestion attempts MUST be logged with retry capability.

### 6.2 AI Processing (Sprint 3)
- FR-05: Each new article MUST be summarized by the configured Ollama model.
- FR-06: Each article MUST receive a one-sentence summary, key points, and a "why it matters" explanation.
- FR-07: Each article MUST be assigned a category from the predefined taxonomy (see §7).
- FR-08: Each article MUST receive an importance score (1–100) from the AI.
- FR-09: AI-generated summaries MUST be stored in a separate `summaries` table linked by article ID.

### 6.3 Dashboard (Sprint 4)
- FR-10: The dashboard MUST display articles with title, summary, category, importance score, and timestamp.
- FR-11: The dashboard MUST support filtering by category.
- FR-12: The dashboard MUST support search by keyword.
- FR-13: The dashboard MUST show article detail with full summary, key points, and why it matters.
- FR-14: The dashboard MUST be usable without login in v1 (local network only).

### 6.4 Daily Digest (Sprint 5)
- FR-15: A digest MUST be generated once per day at a configurable time (default: 07:00 local).
- FR-16: The digest MUST include top stories grouped by category.
- FR-17: The digest MUST be stored in PostgreSQL and viewable in the dashboard.

### 6.5 Semantic Search (Sprint 6)
- FR-18: The system MUST generate vector embeddings for each article summary.
- FR-19: Users MUST be able to find similar articles by semantic similarity.
- FR-20: Users MUST be able to ask natural language questions about news history.

### 6.6 Notifications (Sprint 7)
- FR-21: The system MUST send a daily email digest via AWS SES.
- FR-22: The system MUST support configurable breaking news alerts by category.

---

## 7. Category Taxonomy (v1)

```
AI & Machine Learning
Technology & Software Engineering
Economy & Business
Cybersecurity
Global News & Politics
Health & Medicine
Environment & Climate
Society & Culture
```

---

## 8. Non-Functional Requirements

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-01 | Dashboard initial load time | < 2 seconds |
| NFR-02 | Article summary generation latency | < 60 seconds per article (local LLM) |
| NFR-03 | GDELT ingestion cycle duration | < 5 minutes per cycle |
| NFR-04 | PostgreSQL query response time | < 200ms for dashboard queries |
| NFR-05 | System must run fully offline (no external AI APIs in v1) | Required |
| NFR-06 | All configuration via environment variables | Required |
| NFR-07 | All entities must use UUID primary keys | Required |
| NFR-08 | REST API follows consistent URL structure | Required |
| NFR-09 | AWS-ready: containerized, stateless services | Future |

---

## 9. Constraints & Assumptions

- **GDELT** is the sole data source in v1. It provides structured global event data updated every 15 minutes.
- **Ollama** with **Llama 3.1** runs on the same host as the backend in v1. Model is configurable.
- The system is intended for **single-host local deployment** in v1, with AWS as the target for v2.
- No paid external AI APIs (Claude API, OpenAI) are used in v1.
- The backend is built on **Java 21 + Spring Boot 3** following Clean Architecture principles.

---

## 10. Success Metrics

| Metric | Target |
|--------|--------|
| Articles ingested per day | > 500 |
| AI summaries generated per day | > 500 |
| Daily digest generated on schedule | 100% |
| Dashboard uptime (local) | > 99% during working hours |
| Importance score variance across articles | Non-uniform (not all same score) |
