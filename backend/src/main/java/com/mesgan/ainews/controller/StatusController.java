package com.mesgan.ainews.controller;

import com.mesgan.ainews.dto.StatusResponse;
import com.mesgan.ainews.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatusController {

    private final ArticleRepository articleRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> status() {
        long total = articleRepository.count();
        long processed = articleRepository.countByProcessedTrue();
        long unprocessed = articleRepository.countByProcessedFalse();
        return ResponseEntity.ok(new StatusResponse(total, processed, unprocessed));
    }
}
