package com.mesgan.ainews.mapper;

import com.mesgan.ainews.dto.ArticleDetailResponse;
import com.mesgan.ainews.dto.ArticleResponse;
import com.mesgan.ainews.entity.Article;
import com.mesgan.ainews.entity.Summary;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ArticleMapperTest {

    ArticleMapper mapper = new ArticleMapper();

    // ── toResponse ─────────────────────────────────────────────────────────

    @Test
    void toResponse_withNullSummary_mapsArticleFieldsAndNullsSummary() {
        Article article = buildArticle();

        ArticleResponse result = mapper.toResponse(article, null);

        assertThat(result.id()).isEqualTo(article.getId());
        assertThat(result.title()).isEqualTo(article.getTitle());
        assertThat(result.sourceName()).isEqualTo(article.getSourceName());
        assertThat(result.category()).isEqualTo(article.getCategory());
        assertThat(result.importanceScore()).isEqualTo(article.getImportanceScore());
        assertThat(result.oneSentenceSummary()).isNull();
    }

    @Test
    void toResponse_withSummary_includesOneSentenceSummary() {
        Article article = buildArticle();
        Summary summary = buildSummary(article.getId());

        ArticleResponse result = mapper.toResponse(article, summary);

        assertThat(result.oneSentenceSummary()).isEqualTo(summary.getOneSentenceSummary());
    }

    // ── toDetailResponse ───────────────────────────────────────────────────

    @Test
    void toDetailResponse_withNullSummary_mapsArticleFieldsAndNullsSummaryFields() {
        Article article = buildArticle();

        ArticleDetailResponse result = mapper.toDetailResponse(article, null);

        assertThat(result.id()).isEqualTo(article.getId());
        assertThat(result.url()).isEqualTo(article.getUrl());
        assertThat(result.description()).isEqualTo(article.getDescription());
        assertThat(result.processed()).isEqualTo(article.getProcessed());
        assertThat(result.oneSentenceSummary()).isNull();
        assertThat(result.keyPoints()).isNull();
        assertThat(result.whyItMatters()).isNull();
        assertThat(result.aiCategory()).isNull();
    }

    @Test
    void toDetailResponse_withSummary_mapsAllSummaryFields() {
        Article article = buildArticle();
        Summary summary = buildSummary(article.getId());

        ArticleDetailResponse result = mapper.toDetailResponse(article, summary);

        assertThat(result.oneSentenceSummary()).isEqualTo(summary.getOneSentenceSummary());
        assertThat(result.keyPoints()).isEqualTo(summary.getKeyPoints());
        assertThat(result.whyItMatters()).isEqualTo(summary.getWhyItMatters());
        assertThat(result.aiCategory()).isEqualTo(summary.getAiCategory());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Article buildArticle() {
        return Article.builder()
                .id(UUID.randomUUID())
                .title("Cybersecurity Threat Detected")
                .url("https://secnews.com/threat-alert")
                .sourceName("SecNews")
                .description("A critical vulnerability was discovered.")
                .category("Cybersecurity")
                .importanceScore(95)
                .processed(false)
                .fetchedAt(Instant.now())
                .build();
    }

    private Summary buildSummary(UUID articleId) {
        return Summary.builder()
                .id(UUID.randomUUID())
                .articleId(articleId)
                .oneSentenceSummary("A critical zero-day vulnerability affects millions.")
                .keyPoints("CVE assigned; Patch available; Actively exploited")
                .whyItMatters("Immediate patching required to prevent data breaches.")
                .aiCategory("Cybersecurity")
                .createdAt(Instant.now())
                .build();
    }
}
