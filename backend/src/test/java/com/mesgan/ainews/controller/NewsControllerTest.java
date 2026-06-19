package com.mesgan.ainews.controller;

import com.mesgan.ainews.dto.ArticleDetailResponse;
import com.mesgan.ainews.dto.ArticleResponse;
import com.mesgan.ainews.exception.ResourceNotFoundException;
import com.mesgan.ainews.service.NewsIngestionService;
import com.mesgan.ainews.service.NewsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
class NewsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean NewsService newsService;
    @MockBean NewsIngestionService newsIngestionService;

    @Test
    void getLatestArticles_returnsOkWithPageContent() throws Exception {
        UUID id = UUID.randomUUID();
        ArticleResponse article = new ArticleResponse(
                id, "AI Model Sets New Benchmark", "TechNews",
                "AI & Machine Learning", 90, "AI achieves human-level reasoning.",
                Instant.now(), Instant.now());
        Page<ArticleResponse> page = new PageImpl<>(List.of(article));
        when(newsService.getLatestArticles(0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/news/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.content[0].title").value("AI Model Sets New Benchmark"))
                .andExpect(jsonPath("$.content[0].importanceScore").value(90))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getLatestArticles_withCustomPagination_passesParamsToService() throws Exception {
        when(newsService.getLatestArticles(1, 10)).thenReturn(Page.empty());

        mockMvc.perform(get("/api/news/latest").param("page", "1").param("size", "10"))
                .andExpect(status().isOk());

        verify(newsService).getLatestArticles(1, 10);
    }

    @Test
    void searchArticles_returnsMatchingResults() throws Exception {
        UUID id = UUID.randomUUID();
        ArticleResponse article = new ArticleResponse(
                id, "Spring Boot 3 Deep Dive", "DevBlog",
                "Technology & Software Engineering", 72, null, Instant.now(), Instant.now());
        when(newsService.searchArticles("Spring", 0, 20)).thenReturn(new PageImpl<>(List.of(article)));

        mockMvc.perform(get("/api/news/search").param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot 3 Deep Dive"));
    }

    @Test
    void getArticleById_found_returnsFullDetail() throws Exception {
        UUID id = UUID.randomUUID();
        ArticleDetailResponse detail = new ArticleDetailResponse(
                id, "Cybersecurity Breach at Major Bank", "https://securenews.com/breach",
                "SecureNews", "Full article content here...", "Cybersecurity",
                95, false, Instant.now(), Instant.now(),
                "A major bank suffered a data breach.", "3M records exposed; Swift notified",
                "Immediate threat to banking sector security.", "Cybersecurity");
        when(newsService.getArticleById(id)).thenReturn(detail);

        mockMvc.perform(get("/api/news/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Cybersecurity Breach at Major Bank"))
                .andExpect(jsonPath("$.oneSentenceSummary").value("A major bank suffered a data breach."))
                .andExpect(jsonPath("$.keyPoints").value("3M records exposed; Swift notified"))
                .andExpect(jsonPath("$.whyItMatters").value("Immediate threat to banking sector security."));
    }

    @Test
    void getArticleById_notFound_returns404WithErrorBody() throws Exception {
        UUID id = UUID.randomUUID();
        when(newsService.getArticleById(id))
                .thenThrow(new ResourceNotFoundException("Article not found with id: " + id));

        mockMvc.perform(get("/api/news/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Article not found with id: " + id))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
