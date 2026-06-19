package com.mesgan.ainews.controller;

import com.mesgan.ainews.dto.DigestResponse;
import com.mesgan.ainews.exception.ResourceNotFoundException;
import com.mesgan.ainews.service.DigestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DigestController.class)
class DigestControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean DigestService digestService;

    @Test
    void getTodayDigest_found_returnsOk() throws Exception {
        LocalDate today = LocalDate.now();
        DigestResponse digest = new DigestResponse(
                UUID.randomUUID(), today,
                "Today's top stories: AI, markets, cybersecurity.", Instant.now());
        when(digestService.getTodayDigest()).thenReturn(digest);

        mockMvc.perform(get("/api/digest/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Today's top stories: AI, markets, cybersecurity."));
    }

    @Test
    void getTodayDigest_notFound_returns404() throws Exception {
        LocalDate today = LocalDate.now();
        when(digestService.getTodayDigest())
                .thenThrow(new ResourceNotFoundException("No digest available for today: " + today));

        mockMvc.perform(get("/api/digest/today"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No digest available for today: " + today));
    }

    @Test
    void getDigestByDate_found_returnsOkWithDate() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 1);
        DigestResponse digest = new DigestResponse(
                UUID.randomUUID(), date, "June 1st intelligence report.", Instant.now());
        when(digestService.getDigestByDate(date)).thenReturn(digest);

        mockMvc.perform(get("/api/digest/2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.digestDate").value("2026-06-01"))
                .andExpect(jsonPath("$.content").value("June 1st intelligence report."));
    }

    @Test
    void getDigestByDate_notFound_returns404() throws Exception {
        LocalDate date = LocalDate.of(2026, 1, 1);
        when(digestService.getDigestByDate(date))
                .thenThrow(new ResourceNotFoundException("No digest available for date: " + date));

        mockMvc.perform(get("/api/digest/2026-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
