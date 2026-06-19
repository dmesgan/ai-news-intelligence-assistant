package com.mesgan.ainews.controller;

import com.mesgan.ainews.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatusController.class)
class StatusControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean ArticleRepository articleRepository;

    @Test
    void health_returnsUpStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void status_returnsArticleCounts() throws Exception {
        when(articleRepository.count()).thenReturn(150L);
        when(articleRepository.countByProcessedTrue()).thenReturn(120L);
        when(articleRepository.countByProcessedFalse()).thenReturn(30L);

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalArticles").value(150))
                .andExpect(jsonPath("$.processedArticles").value(120))
                .andExpect(jsonPath("$.unprocessedArticles").value(30));
    }

    @Test
    void status_emptyDatabase_returnsZeroCounts() throws Exception {
        when(articleRepository.count()).thenReturn(0L);
        when(articleRepository.countByProcessedTrue()).thenReturn(0L);
        when(articleRepository.countByProcessedFalse()).thenReturn(0L);

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalArticles").value(0))
                .andExpect(jsonPath("$.processedArticles").value(0))
                .andExpect(jsonPath("$.unprocessedArticles").value(0));
    }
}
