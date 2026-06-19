package com.mesgan.ainews.controller;

import com.mesgan.ainews.dto.DigestResponse;
import com.mesgan.ainews.service.DigestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/digest")
@RequiredArgsConstructor
public class DigestController {

    private final DigestService digestService;

    @GetMapping("/today")
    public ResponseEntity<DigestResponse> getTodayDigest() {
        return ResponseEntity.ok(digestService.getTodayDigest());
    }

    @GetMapping("/{date}")
    public ResponseEntity<DigestResponse> getDigestByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(digestService.getDigestByDate(date));
    }
}
