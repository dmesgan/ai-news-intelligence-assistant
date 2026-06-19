package com.mesgan.ainews.dto;

import java.time.Instant;

/**
 * Internal DTO carrying one parsed row from the GDELT GKG file.
 * Never returned to API clients — mapped to Article entity inside NewsIngestionService.
 */
public record GdeltArticleDto(
        String url,
        String sourceName,
        String themes,
        float tone,
        Instant publishedAt
) {}
