package com.mesgan.ainews.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "articles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(unique = true, nullable = false)
    private String url;

    private String sourceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String category;

    @Builder.Default
    private Integer importanceScore = 0;

    @Builder.Default
    private Boolean processed = false;

    private Instant publishedAt;

    @Builder.Default
    private Instant fetchedAt = Instant.now();
}
