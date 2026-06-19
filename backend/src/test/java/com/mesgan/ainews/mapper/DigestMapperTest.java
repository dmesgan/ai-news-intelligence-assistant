package com.mesgan.ainews.mapper;

import com.mesgan.ainews.dto.DigestResponse;
import com.mesgan.ainews.entity.DailyDigest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class DigestMapperTest {

    DigestMapper mapper = new DigestMapper();

    @Test
    void toResponse_mapsAllFields() {
        DailyDigest digest = DailyDigest.builder()
                .id(UUID.randomUUID())
                .digestDate(LocalDate.of(2026, 6, 19))
                .content("Top stories: AI breakthrough leads today's headlines.")
                .createdAt(Instant.now())
                .build();

        DigestResponse result = mapper.toResponse(digest);

        assertThat(result.id()).isEqualTo(digest.getId());
        assertThat(result.digestDate()).isEqualTo(digest.getDigestDate());
        assertThat(result.content()).isEqualTo(digest.getContent());
        assertThat(result.createdAt()).isEqualTo(digest.getCreatedAt());
    }
}
