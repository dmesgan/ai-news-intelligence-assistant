package com.mesgan.ainews.repository;

import com.mesgan.ainews.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    boolean existsByUrl(String url);

    long countByProcessedTrue();

    long countByProcessedFalse();

    // Used by SummaryService — fetch next batch for AI processing
    Page<Article> findByProcessedFalse(Pageable pageable);

    // Single query for all existing URLs — fixes N+1 in duplicate detection
    @Query("SELECT a.url FROM Article a")
    List<String> findAllUrls();

    @Query("SELECT a FROM Article a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
