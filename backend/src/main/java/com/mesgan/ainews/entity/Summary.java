package com.mesgan.ainews.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "summaries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(columnDefinition = "TEXT")
    private String oneSentenceSummary;

    @Column(columnDefinition = "TEXT")
    private String keyPoints;

    @Column(columnDefinition = "TEXT")
    private String whyItMatters;

    private String aiCategory;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
