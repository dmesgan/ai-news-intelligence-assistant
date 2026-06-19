package com.mesgan.ainews.service;

import com.mesgan.ainews.dto.ArticleDetailResponse;
import com.mesgan.ainews.dto.ArticleResponse;
import com.mesgan.ainews.entity.Article;
import com.mesgan.ainews.entity.Summary;
import com.mesgan.ainews.exception.ResourceNotFoundException;
import com.mesgan.ainews.mapper.ArticleMapper;
import com.mesgan.ainews.repository.ArticleRepository;
import com.mesgan.ainews.repository.SummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock ArticleRepository articleRepository;
    @Mock SummaryRepository summaryRepository;
    @Mock ArticleMapper articleMapper;

    @InjectMocks NewsService newsService;

    // ── getLatestArticles ──────────────────────────────────────────────────

    @Test
    void getLatestArticles_returnsPageOfResponses() {
        Article article = buildArticle();
        ArticleResponse response = buildArticleResponse(article.getId());
        when(articleRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(article)));
        when(summaryRepository.findByArticleId(article.getId())).thenReturn(Optional.empty());
        when(articleMapper.toResponse(article, null)).thenReturn(response);

        Page<ArticleResponse> result = newsService.getLatestArticles(0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
    }

    @Test
    void getLatestArticles_withSummary_passesSummaryToMapper() {
        Article article = buildArticle();
        Summary summary = buildSummary(article.getId());
        when(articleRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(article)));
        when(summaryRepository.findByArticleId(article.getId())).thenReturn(Optional.of(summary));
        when(articleMapper.toResponse(article, summary)).thenReturn(buildArticleResponse(article.getId()));

        newsService.getLatestArticles(0, 20);

        verify(articleMapper).toResponse(article, summary);
    }

    @Test
    void getLatestArticles_emptyDatabase_returnsEmptyPage() {
        when(articleRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        Page<ArticleResponse> result = newsService.getLatestArticles(0, 20);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ── searchArticles ─────────────────────────────────────────────────────

    @Test
    void searchArticles_returnsMatchingResults() {
        Article article = buildArticle();
        ArticleResponse response = buildArticleResponse(article.getId());
        when(articleRepository.searchByKeyword(eq("java"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(article)));
        when(summaryRepository.findByArticleId(article.getId())).thenReturn(Optional.empty());
        when(articleMapper.toResponse(article, null)).thenReturn(response);

        Page<ArticleResponse> result = newsService.searchArticles("java", 0, 20);

        assertThat(result.getContent()).hasSize(1);
        verify(articleRepository).searchByKeyword(eq("java"), any(Pageable.class));
    }

    @Test
    void searchArticles_noMatches_returnsEmptyPage() {
        when(articleRepository.searchByKeyword(eq("xyz"), any(Pageable.class))).thenReturn(Page.empty());

        Page<ArticleResponse> result = newsService.searchArticles("xyz", 0, 20);

        assertThat(result.getContent()).isEmpty();
    }

    // ── getArticleById ─────────────────────────────────────────────────────

    @Test
    void getArticleById_found_returnsDetailResponse() {
        UUID id = UUID.randomUUID();
        Article article = buildArticle();
        ArticleDetailResponse detail = buildArticleDetailResponse(id);
        when(articleRepository.findById(id)).thenReturn(Optional.of(article));
        when(summaryRepository.findByArticleId(id)).thenReturn(Optional.empty());
        when(articleMapper.toDetailResponse(article, null)).thenReturn(detail);

        ArticleDetailResponse result = newsService.getArticleById(id);

        assertThat(result).isEqualTo(detail);
    }

    @Test
    void getArticleById_withSummary_passesSummaryToMapper() {
        UUID id = UUID.randomUUID();
        Article article = buildArticle();
        Summary summary = buildSummary(id);
        when(articleRepository.findById(id)).thenReturn(Optional.of(article));
        when(summaryRepository.findByArticleId(id)).thenReturn(Optional.of(summary));
        when(articleMapper.toDetailResponse(article, summary)).thenReturn(buildArticleDetailResponse(id));

        newsService.getArticleById(id);

        verify(articleMapper).toDetailResponse(article, summary);
    }

    @Test
    void getArticleById_notFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(articleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.getArticleById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Article buildArticle() {
        return Article.builder()
                .id(UUID.randomUUID())
                .title("AI Model Breaks Records")
                .url("https://technews.com/ai-breakthrough")
                .sourceName("TechNews")
                .category("AI & Machine Learning")
                .importanceScore(88)
                .processed(false)
                .fetchedAt(Instant.now())
                .build();
    }

    private Summary buildSummary(UUID articleId) {
        return Summary.builder()
                .id(UUID.randomUUID())
                .articleId(articleId)
                .oneSentenceSummary("A new AI model sets a benchmark record.")
                .keyPoints("Record performance; Open-source release; Industry impact")
                .whyItMatters("Accelerates AI adoption across industries.")
                .aiCategory("AI & Machine Learning")
                .createdAt(Instant.now())
                .build();
    }

    private ArticleResponse buildArticleResponse(UUID id) {
        return new ArticleResponse(id, "AI Model Breaks Records", "TechNews",
                "AI & Machine Learning", 88, null, Instant.now(), Instant.now());
    }

    private ArticleDetailResponse buildArticleDetailResponse(UUID id) {
        return new ArticleDetailResponse(id, "AI Model Breaks Records",
                "https://technews.com/ai-breakthrough", "TechNews", "Full article content...",
                "AI & Machine Learning", 88, false, Instant.now(), Instant.now(),
                null, null, null, null);
    }
}
