package com.mesgan.ainews.dto;

public record OllamaGenerateResponse(
        String model,
        String response,
        boolean done
) {}
