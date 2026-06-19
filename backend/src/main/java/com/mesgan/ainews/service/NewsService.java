package com.mesgan.ainews.service;

import com.mesgan.ainews.dto.ArticleDetailResponse;
import com.mesgan.ainews.dto.ArticleResponse;
import com.mesgan.ainews.entity.Article;
import com.mesgan.ainews.entity.Summary;
import com.mesgan.ainews.exception.ResourceNotFoundException;
import com.mesgan.ainews.mapper.ArticleMapper;
import com.mesgan.ainews.repository.ArticleRepository;
import com.mesgan.ainews.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NewsService {

    private final ArticleRepository articleRepository;
    private final SummaryRepository summaryRepository;
    private final ArticleMapper articleMapper;

    public Page<ArticleResponse> getLatestArticles(int page, int size) {
        log.info("Fetching latest articles - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "importanceScore", "fetchedAt"));
        // Note: summary fetched per article — optimize with JOIN query in Sprint 3
        return articleRepository.findAll(pageable)
                .map(article -> {
                    Summary summary = summaryRepository.findByArticleId(article.getId()).orElse(null);
                    return articleMapper.toResponse(article, summary);
                });
    }

    public Page<ArticleResponse> searchArticles(String keyword, int page, int size) {
        log.info("Searching articles with keyword: '{}'", keyword);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "importanceScore"));
        return articleRepository.searchByKeyword(keyword, pageable)
                .map(article -> {
                    Summary summary = summaryRepository.findByArticleId(article.getId()).orElse(null);
                    return articleMapper.toResponse(article, summary);
                });
    }

    public ArticleDetailResponse getArticleById(UUID id) {
        log.info("Fetching article by id: {}", id);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
        Summary summary = summaryRepository.findByArticleId(id).orElse(null);
        return articleMapper.toDetailResponse(article, summary);
    }
}
