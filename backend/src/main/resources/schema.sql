CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS articles (
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

CREATE INDEX IF NOT EXISTS idx_articles_processed  ON articles(processed);
CREATE INDEX IF NOT EXISTS idx_articles_importance ON articles(importance_score DESC);
CREATE INDEX IF NOT EXISTS idx_articles_published  ON articles(published_at DESC);
CREATE INDEX IF NOT EXISTS idx_articles_category   ON articles(category);

CREATE TABLE IF NOT EXISTS summaries (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    article_id           UUID NOT NULL REFERENCES articles(id),
    one_sentence_summary TEXT,
    key_points           TEXT,
    why_it_matters       TEXT,
    ai_category          TEXT,
    created_at           TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_summaries_article_id ON summaries(article_id);

CREATE TABLE IF NOT EXISTS daily_digests (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    digest_date  DATE UNIQUE NOT NULL,
    content      TEXT NOT NULL,
    created_at   TIMESTAMPTZ DEFAULT NOW()
);
