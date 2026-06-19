package com.mesgan.ainews.dto;

import java.time.Instant;
import java.util.UUID;

public record ArticleDetailResponse(
        UUID id,
        String title,
        String url,
        String sourceName,
        String description,
        String category,
        Integer importanceScore,
        Boolean processed,
        Instant publishedAt,
        Instant fetchedAt,
        String oneSentenceSummary,
        String keyPoints,
        String whyItMatters,
        String aiCategory
) {}
