package com.mesgan.ainews.repository;

import com.mesgan.ainews.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    boolean existsByUrl(String url);

    long countByProcessedTrue();

    long countByProcessedFalse();

    @Query("SELECT a FROM Article a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
