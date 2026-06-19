package com.mesgan.ainews.dto;

public record OllamaGenerateRequest(
        String model,
        String prompt,
        boolean stream
) {}
