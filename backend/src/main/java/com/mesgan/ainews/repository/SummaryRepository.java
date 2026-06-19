package com.mesgan.ainews.repository;

import com.mesgan.ainews.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SummaryRepository extends JpaRepository<Summary, UUID> {

    Optional<Summary> findByArticleId(UUID articleId);
}
