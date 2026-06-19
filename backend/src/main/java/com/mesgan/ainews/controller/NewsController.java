package com.mesgan.ainews.controller;

import com.mesgan.ainews.dto.ArticleDetailResponse;
import com.mesgan.ainews.dto.ArticleResponse;
import com.mesgan.ainews.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/latest")
    public ResponseEntity<Page<ArticleResponse>> getLatestArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(newsService.getLatestArticles(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ArticleResponse>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(newsService.searchArticles(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDetailResponse> getArticleById(@PathVariable UUID id) {
        return ResponseEntity.ok(newsService.getArticleById(id));
    }
}
