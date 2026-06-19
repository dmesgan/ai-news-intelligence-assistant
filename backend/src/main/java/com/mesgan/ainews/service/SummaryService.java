package com.mesgan.ainews.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mesgan.ainews.client.OllamaClient;
import com.mesgan.ainews.dto.AiSummaryDto;
import com.mesgan.ainews.dto.ProcessingResultDto;
import com.mesgan.ainews.entity.Article;
import com.mesgan.ainews.entity.Summary;
import com.mesgan.ainews.repository.ArticleRepository;
import com.mesgan.ainews.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private static final Set<String> VALID_CATEGORIES = Set.of(
            "AI & Machine Learning",
            "Technology & Software Engineering",
            "Economy & Business",
            "Cybersecurity",
            "Global News & Politics",
            "Health & Medicine",
            "Environment & Climate",
            "Society & Culture"
    );

    private final ArticleRepository articleRepository;
    private final SummaryRepository summaryRepository;
    private final OllamaClient ollamaClient;
    private final PromptBuilderService promptBuilderService;
    private final ObjectMapper objectMapper;

    public ProcessingResultDto processUnprocessedArticles(int batchSize) {
        long startTime = System.currentTimeMillis();

        List<Article> articles = articleRepository
                .findByProcessedFalse(PageRequest.of(0, batchSize))
                .getContent();

        log.info("Starting AI processing — {} articles in batch", articles.size());

        int processed = 0;
        int failed = 0;

        for (Article article : articles) {
            try {
                processOne(article);
                processed++;
            } catch (Exception e) {
                log.error("Failed to process article {} — {}", article.getId(), e.getMessage());
                failed++;
            }
        }

        long durationMs = System.currentTimeMillis() - startTime;
        log.info("AI processing complete — processed: {}, failed: {}, duration: {}ms",
                processed, failed, durationMs);

        return new ProcessingResultDto(processed, failed, durationMs, "success");
    }

    private void processOne(Article article) {
        // Idempotency: skip if summary already exists (handles retry after partial failure)
        if (summaryRepository.findByArticleId(article.getId()).isPresent()) {
            log.info("Summary already exists for article {}, marking processed", article.getId());
            article.setProcessed(true);
            articleRepository.save(article);
            return;
        }

        Optional<String> rawResponse = ollamaClient.generate(
                promptBuilderService.buildSummaryPrompt(article));

        if (rawResponse.isEmpty()) {
            log.warn("Ollama returned no response for article {} — will retry next cycle", article.getId());
            return; // leave processed=false so scheduler retries
        }

        AiSummaryDto aiSummary = parseResponse(rawResponse.get());

        Summary summary = Summary.builder()
                .articleId(article.getId())
                .oneSentenceSummary(aiSummary.oneSentenceSummary())
                .keyPoints(aiSummary.keyPoints())
                .whyItMatters(aiSummary.whyItMatters())
                .aiCategory(validateCategory(aiSummary.aiCategory()))
                .build();
        summaryRepository.save(summary);

        article.setTitle(aiSummary.title());
        article.setCategory(validateCategory(aiSummary.aiCategory()));
        article.setImportanceScore(validateScore(aiSummary.importanceScore()));
        article.setProcessed(true);
        articleRepository.save(article);

        log.debug("Processed article {} — score: {}, category: {}",
                article.getId(), article.getImportanceScore(), article.getCategory());
    }

    private AiSummaryDto parseResponse(String raw) {
        try {
            String json = extractJson(raw);
            return objectMapper.readValue(json, AiSummaryDto.class);
        } catch (Exception e) {
            log.warn("Failed to parse Ollama JSON response, using fallback. Error: {}", e.getMessage());
            return fallback();
        }
    }

    // Model may add prose before/after the JSON block — extract just the JSON object
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end   = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String validateCategory(String category) {
        if (category != null && VALID_CATEGORIES.contains(category)) {
            return category;
        }
        log.debug("Invalid aiCategory '{}' returned by model — defaulting", category);
        return "Global News & Politics";
    }

    private int validateScore(Integer score) {
        if (score == null) return 50;
        return Math.min(100, Math.max(1, score));
    }

    private AiSummaryDto fallback() {
        return new AiSummaryDto(
                "News Article",
                "Summary generation pending.",
                "• Details pending AI analysis",
                "Significance analysis pending.",
                "Global News & Politics",
                50
        );
    }
}
