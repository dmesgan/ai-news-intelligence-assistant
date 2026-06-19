package com.mesgan.ainews.dto;

public record IngestionResultDto(
        int articlesFound,
        int articlesNew,
        int articlesDuplicate,
        long durationMs,
        String status
) {}
