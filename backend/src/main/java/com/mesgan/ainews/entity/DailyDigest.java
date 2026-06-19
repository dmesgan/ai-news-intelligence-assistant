package com.mesgan.ainews.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_digests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDigest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private LocalDate digestDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
