package com.mesgan.ainews.service;

import com.mesgan.ainews.dto.DigestResponse;
import com.mesgan.ainews.entity.DailyDigest;
import com.mesgan.ainews.exception.ResourceNotFoundException;
import com.mesgan.ainews.mapper.DigestMapper;
import com.mesgan.ainews.repository.DailyDigestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DigestService {

    private final DailyDigestRepository dailyDigestRepository;
    private final DigestMapper digestMapper;

    public DigestResponse getTodayDigest() {
        LocalDate today = LocalDate.now();
        log.info("Fetching digest for today: {}", today);
        DailyDigest digest = dailyDigestRepository.findByDigestDate(today)
                .orElseThrow(() -> new ResourceNotFoundException("No digest available for today: " + today));
        return digestMapper.toResponse(digest);
    }

    public DigestResponse getDigestByDate(LocalDate date) {
        log.info("Fetching digest for date: {}", date);
        DailyDigest digest = dailyDigestRepository.findByDigestDate(date)
                .orElseThrow(() -> new ResourceNotFoundException("No digest available for date: " + date));
        return digestMapper.toResponse(digest);
    }
}
