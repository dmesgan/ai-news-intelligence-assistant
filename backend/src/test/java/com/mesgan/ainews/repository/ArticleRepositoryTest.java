package com.mesgan.ainews.repository;

import com.mesgan.ainews.entity.Article;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

// Use H2 in-memory. Hibernate creates tables from entities (create-drop).
// Schema.sql disabled because it contains PostgreSQL-specific syntax H2 can't parse.
@DataJpaTest
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ArticleRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired ArticleRepository articleRepository;

    // ── existsByUrl ────────────────────────────────────────────────────────

    @Test
    void existsByUrl_existingUrl_returnsTrue() {
        em.persistAndFlush(article("https://technews.com/story-1"));

        assertThat(articleRepository.existsByUrl("https://technews.com/story-1")).isTrue();
    }

    @Test
    void existsByUrl_nonExistingUrl_returnsFalse() {
        assertThat(articleRepository.existsByUrl("https://technews.com/does-not-exist")).isFalse();
    }

    // ── countByProcessed ───────────────────────────────────────────────────

    @Test
    void countByProcessedTrue_returnsOnlyProcessedCount() {
        em.persistAndFlush(article("https://a.com/1", true));
        em.persistAndFlush(article("https://a.com/2", true));
        em.persistAndFlush(article("https://a.com/3", false));

        assertThat(articleRepository.countByProcessedTrue()).isEqualTo(2);
    }

    @Test
    void countByProcessedFalse_returnsOnlyUnprocessedCount() {
        em.persistAndFlush(article("https://b.com/1", true));
        em.persistAndFlush(article("https://b.com/2", false));
        em.persistAndFlush(article("https://b.com/3", false));

        assertThat(articleRepository.countByProcessedFalse()).isEqualTo(2);
    }

    // ── searchByKeyword ────────────────────────────────────────────────────

    @Test
    void searchByKeyword_matchesTitle_returnsArticle() {
        em.persistAndFlush(articleWithTitle("https://c.com/1", "Spring Boot Advanced Guide"));
        em.persistAndFlush(articleWithTitle("https://c.com/2", "Python Data Science Handbook"));

        Page<Article> result = articleRepository.searchByKeyword("Spring", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Spring Boot Advanced Guide");
    }

    @Test
    void searchByKeyword_matchesDescription_returnsArticle() {
        Article a = Article.builder()
                .url("https://d.com/1")
                .title("Tech News")
                .description("A deep dive into machine learning algorithms and neural networks")
                .processed(false)
                .build();
        em.persistAndFlush(a);

        Page<Article> result = articleRepository.searchByKeyword("machine learning", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchByKeyword_caseInsensitive_findsMatch() {
        em.persistAndFlush(articleWithTitle("https://e.com/1", "Cybersecurity Threat Alert"));

        Page<Article> result = articleRepository.searchByKeyword("CYBERSECURITY", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Cybersecurity Threat Alert");
    }

    @Test
    void searchByKeyword_noMatch_returnsEmptyPage() {
        em.persistAndFlush(articleWithTitle("https://f.com/1", "Economy Update"));

        Page<Article> result = articleRepository.searchByKeyword("quantum", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Article article(String url) {
        return Article.builder().url(url).title("Test Article").processed(false).build();
    }

    private Article article(String url, boolean processed) {
        return Article.builder().url(url).title("Test Article").processed(processed).build();
    }

    private Article articleWithTitle(String url, String title) {
        return Article.builder().url(url).title(title).processed(false).build();
    }
}
