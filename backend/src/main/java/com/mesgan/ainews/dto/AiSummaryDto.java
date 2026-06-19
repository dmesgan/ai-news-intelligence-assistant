package com.mesgan.ainews.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Parsed JSON output from Ollama. Maps directly from the model's response string.
 * JsonIgnoreProperties: tolerates extra fields the model adds unexpectedly.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiSummaryDto(
        String title,
        String oneSentenceSummary,
        String keyPoints,
        String whyItMatters,
        String aiCategory,
        Integer importanceScore
) {}
