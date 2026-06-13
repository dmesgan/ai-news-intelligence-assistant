# Project Vision — AI News Intelligence Assistant

## 1. Overview

The AI News Intelligence Assistant is a self-hosted news aggregation and intelligence platform that ingests global news events from GDELT, processes them with a local LLM (Ollama), and surfaces concise, ranked, and categorized summaries through a React dashboard. A daily digest is generated automatically to keep users informed without information overload.

---

## 2. Problem Statement

Global news is high-volume, repetitive, and often lacks context. Existing aggregators (Google News, Feedly) are cloud-dependent, opaque in their ranking logic, and do not allow custom AI-driven summarization. This project gives a single user (or small team) full control over collection, enrichment, and presentation of news intelligence — with no external AI API costs.

---

## 3. Goals

| # | Goal | Priority |
|---|------|----------|
| G1 | Collect structured news events from GDELT continuously | Must Have |
| G2 | Persist articles and metadata in PostgreSQL | Must Have |
| G3 | Summarize each article using a local Ollama LLM | Must Have |
| G4 | Categorize articles into meaningful topic domains | Must Have |
| G5 | Rank articles by relevance and recency | Must Have |
| G6 | Display ranked, summarized news in a React dashboard | Must Have |
| G7 | Generate and store a daily digest | Must Have |
| G8 | Deploy the complete stack to AWS | Future |

---

## 4. Non-Goals (v1)

- Multi-user authentication and role-based access
- Mobile native app
- News source beyond GDELT (RSS, Twitter/X, Webhooks)
- Real-time push notifications
- Fine-tuning or training custom models

---

## 5. Users

**Primary User:** A solo analyst or developer who wants a self-hosted, AI-assisted news briefing every day without vendor lock-in.

**Use Cases:**

| ID | As a user, I want to… | So that… |
|----|----------------------|----------|
| UC1 | See today's top news ranked by relevance | I can quickly scan what matters |
| UC2 | Read a 2–3 sentence AI summary per article | I don't have to read full articles |
| UC3 | Filter news by category (Politics, Tech, Economy…) | I can focus on specific domains |
| UC4 | Receive a daily digest of top stories | I stay informed without active monitoring |
| UC5 | Know when data was last collected | I can trust the freshness of information |

---

## 6. Functional Requirements

### 6.1 Data Ingestion
- FR-01: The system MUST poll GDELT 2.0 on a configurable schedule (default: every 15 minutes).
- FR-02: The system MUST deduplicate articles by URL before storage.
- FR-03: The system MUST store raw GDELT event metadata alongside article content.
- FR-04: Failed ingestion attempts MUST be logged with retry capability.

### 6.2 AI Processing
- FR-05: Each new article MUST be summarized by the configured Ollama model within 5 minutes of ingestion.
- FR-06: Each article MUST be assigned a category from a predefined taxonomy (see §7).
- FR-07: Summaries MUST be stored in PostgreSQL alongside the source article.
- FR-08: Processing MUST be asynchronous and non-blocking to ingestion.

### 6.3 Ranking
- FR-09: Articles MUST receive a composite rank score combining recency, GDELT tone score, and source diversity.
- FR-10: Rank scores MUST be recalculated on each ingestion cycle.

### 6.4 Dashboard
- FR-11: The dashboard MUST display ranked articles with title, summary, category, score, and timestamp.
- FR-12: The dashboard MUST support filtering by category and date range.
- FR-13: The dashboard MUST show the last ingestion timestamp.
- FR-14: The dashboard MUST be usable without login in v1 (local network only).

### 6.5 Daily Digest
- FR-15: A digest MUST be generated once per day at a configurable time (default: 07:00 local).
- FR-16: The digest MUST include the top N articles (default: 10) ranked by the daily score.
- FR-17: The digest MUST be stored in PostgreSQL and viewable in the dashboard.

---

## 7. Category Taxonomy (v1)

```
Politics & Governance
Economy & Finance
Technology & Science
Health & Medicine
Environment & Climate
Conflict & Security
Society & Culture
Business & Markets
```

---

## 8. Non-Functional Requirements

| ID | Requirement | Target |
|----|-------------|--------|
| NFR-01 | Dashboard initial load time | < 2 seconds |
| NFR-02 | Article summary generation latency | < 60 seconds per article (local LLM) |
| NFR-03 | GDELT ingestion cycle duration | < 5 minutes per cycle |
| NFR-04 | PostgreSQL query response time | < 200ms for dashboard queries |
| NFR-05 | System must run fully offline (no external AI APIs) | Required |
| NFR-06 | All configuration via environment variables | Required |
| NFR-07 | AWS-ready: containerized, stateless services | Future |

---

## 9. Constraints & Assumptions

- **GDELT** is the sole data source in v1. It provides structured global event data updated every 15 minutes.
- **Ollama** runs on the same host as the backend in v1. Model choice (e.g., Llama 3, Mistral) is configurable.
- The system is intended for **single-host local deployment** in v1, with AWS as the target for v2.
- No paid external APIs are used anywhere in v1.

---

## 10. Success Metrics

| Metric | Target |
|--------|--------|
| Articles ingested per day | > 500 |
| Summaries generated per day | > 500 |
| Daily digest generated on schedule | 100% |
| Dashboard uptime (local) | > 99% during working hours |
