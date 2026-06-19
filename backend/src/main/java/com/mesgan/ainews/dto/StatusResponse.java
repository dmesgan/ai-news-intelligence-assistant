package com.mesgan.ainews.dto;

public record StatusResponse(
        long totalArticles,
        long processedArticles,
        long unprocessedArticles
) {}
