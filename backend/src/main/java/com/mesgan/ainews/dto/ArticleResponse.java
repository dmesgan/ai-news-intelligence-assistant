package com.mesgan.ainews.dto;

import java.time.Instant;
import java.util.UUID;

public record ArticleResponse(
        UUID id,
        String title,
        String sourceName,
        String category,
        Integer importanceScore,
        String oneSentenceSummary,
        Instant publishedAt,
        Instant fetchedAt
) {}
