package com.mesgan.ainews.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DigestResponse(
        UUID id,
        LocalDate digestDate,
        String content,
        Instant createdAt
) {}
