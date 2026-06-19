package com.mesgan.ainews.service;

import com.mesgan.ainews.dto.DigestResponse;
import com.mesgan.ainews.entity.DailyDigest;
import com.mesgan.ainews.exception.ResourceNotFoundException;
import com.mesgan.ainews.mapper.DigestMapper;
import com.mesgan.ainews.repository.DailyDigestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigestServiceTest {

    @Mock DailyDigestRepository dailyDigestRepository;
    @Mock DigestMapper digestMapper;

    @InjectMocks DigestService digestService;

    // ── getTodayDigest ─────────────────────────────────────────────────────

    @Test
    void getTodayDigest_found_returnsResponse() {
        LocalDate today = LocalDate.now();
        DailyDigest digest = buildDigest(today);
        DigestResponse response = buildDigestResponse(digest.getId(), today);
        when(dailyDigestRepository.findByDigestDate(today)).thenReturn(Optional.of(digest));
        when(digestMapper.toResponse(digest)).thenReturn(response);

        DigestResponse result = digestService.getTodayDigest();

        assertThat(result).isEqualTo(response);
        verify(dailyDigestRepository).findByDigestDate(today);
    }

    @Test
    void getTodayDigest_notFound_throwsResourceNotFoundException() {
        LocalDate today = LocalDate.now();
        when(dailyDigestRepository.findByDigestDate(today)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digestService.getTodayDigest())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(today.toString());
    }

    // ── getDigestByDate ────────────────────────────────────────────────────

    @Test
    void getDigestByDate_found_returnsResponse() {
        LocalDate date = LocalDate.of(2026, 6, 1);
        DailyDigest digest = buildDigest(date);
        DigestResponse response = buildDigestResponse(digest.getId(), date);
        when(dailyDigestRepository.findByDigestDate(date)).thenReturn(Optional.of(digest));
        when(digestMapper.toResponse(digest)).thenReturn(response);

        DigestResponse result = digestService.getDigestByDate(date);

        assertThat(result).isEqualTo(response);
        verify(dailyDigestRepository).findByDigestDate(date);
    }

    @Test
    void getDigestByDate_notFound_throwsResourceNotFoundException() {
        LocalDate date = LocalDate.of(2026, 6, 1);
        when(dailyDigestRepository.findByDigestDate(date)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> digestService.getDigestByDate(date))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(date.toString());
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private DailyDigest buildDigest(LocalDate date) {
        return DailyDigest.builder()
                .id(UUID.randomUUID())
                .digestDate(date)
                .content("Top stories: AI advancement, market rally, cybersecurity breach.")
                .createdAt(Instant.now())
                .build();
    }

    private DigestResponse buildDigestResponse(UUID id, LocalDate date) {
        return new DigestResponse(id, date,
                "Top stories: AI advancement, market rally, cybersecurity breach.", Instant.now());
    }
}
