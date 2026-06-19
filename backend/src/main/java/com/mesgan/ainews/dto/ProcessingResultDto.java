package com.mesgan.ainews.dto;

public record ProcessingResultDto(
        int articlesProcessed,
        int articlesFailed,
        long durationMs,
        String status
) {}
