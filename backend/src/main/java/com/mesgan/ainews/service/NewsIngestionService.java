package com.mesgan.ainews.service;

import com.mesgan.ainews.client.GdeltClient;
import com.mesgan.ainews.dto.GdeltArticleDto;
import com.mesgan.ainews.dto.IngestionResultDto;
import com.mesgan.ainews.entity.Article;
import com.mesgan.ainews.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsIngestionService {

    private final GdeltClient gdeltClient;
    private final ArticleRepository articleRepository;

    @Transactional
    public IngestionResultDto ingestLatest() {
        long startTime = System.currentTimeMillis();
        log.info("Starting GDELT ingestion cycle");

        List<GdeltArticleDto> fetched = gdeltClient.fetchLatestArticles();
        int found = fetched.size();

        List<Article> toSave = new ArrayList<>();
        for (GdeltArticleDto dto : fetched) {
            if (!articleRepository.existsByUrl(dto.url())) {
                toSave.add(toArticle(dto));
            }
        }

        if (!toSave.isEmpty()) {
            articleRepository.saveAll(toSave);
        }

        int newCount      = toSave.size();
        int duplicates    = found - newCount;
        long durationMs   = System.currentTimeMillis() - startTime;

        log.info("Ingestion complete — found: {}, new: {}, duplicates: {}, duration: {}ms",
                found, newCount, duplicates, durationMs);

        return new IngestionResultDto(found, newCount, duplicates, durationMs, "success");
    }

    private Article toArticle(GdeltArticleDto dto) {
        return Article.builder()
                .url(dto.url())
                .sourceName(dto.sourceName())
                .category(mapCategory(dto.themes()))
                .importanceScore(0)   // Ollama will score in Sprint 3
                .processed(false)
                .publishedAt(dto.publishedAt())
                .fetchedAt(Instant.now())
                .build();
    }

    // Maps GDELT themes (e.g. "ECON_INFLATION;CYBER_ATTACK") to our taxonomy
    private String mapCategory(String themes) {
        if (themes == null || themes.isBlank()) return "Global News & Politics";
        String upper = themes.toUpperCase();
        if (upper.contains("AI_") || upper.contains("ARTIFICIAL_INTEL") || upper.contains("MACHINE_LEARNING"))
            return "AI & Machine Learning";
        if (upper.contains("CYBER") || upper.contains("HACKING") || upper.contains("DATA_BREACH"))
            return "Cybersecurity";
        if (upper.contains("TECH_") || upper.contains("SCIENCE_") || upper.contains("SOFT_") || upper.contains("COMPUTING"))
            return "Technology & Software Engineering";
        if (upper.contains("ECON_") || upper.contains("BUS_") || upper.contains("MARKET") || upper.contains("FINANCE"))
            return "Economy & Business";
        if (upper.contains("HEALTH_") || upper.contains("MED_") || upper.contains("DISEASE") || upper.contains("HOSPITAL"))
            return "Health & Medicine";
        if (upper.contains("ENV_") || upper.contains("CLIMATE") || upper.contains("ENVIRONMENT"))
            return "Environment & Climate";
        return "Global News & Politics";
    }
}
