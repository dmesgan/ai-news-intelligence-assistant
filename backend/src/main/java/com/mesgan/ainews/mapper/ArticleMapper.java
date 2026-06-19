package com.mesgan.ainews.mapper;

import com.mesgan.ainews.dto.ArticleDetailResponse;
import com.mesgan.ainews.dto.ArticleResponse;
import com.mesgan.ainews.entity.Article;
import com.mesgan.ainews.entity.Summary;
import org.springframework.stereotype.Component;

@Component
public class ArticleMapper {

    public ArticleResponse toResponse(Article article, Summary summary) {
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getSourceName(),
                article.getCategory(),
                article.getImportanceScore(),
                summary != null ? summary.getOneSentenceSummary() : null,
                article.getPublishedAt(),
                article.getFetchedAt()
        );
    }

    public ArticleDetailResponse toDetailResponse(Article article, Summary summary) {
        return new ArticleDetailResponse(
                article.getId(),
                article.getTitle(),
                article.getUrl(),
                article.getSourceName(),
                article.getDescription(),
                article.getCategory(),
                article.getImportanceScore(),
                article.getProcessed(),
                article.getPublishedAt(),
                article.getFetchedAt(),
                summary != null ? summary.getOneSentenceSummary() : null,
                summary != null ? summary.getKeyPoints() : null,
                summary != null ? summary.getWhyItMatters() : null,
                summary != null ? summary.getAiCategory() : null
        );
    }
}
