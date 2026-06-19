package com.mesgan.ainews.scheduler;

import com.mesgan.ainews.service.NewsIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GdeltCollectorScheduler {

    private final NewsIngestionService newsIngestionService;

    // Fixed delay: next run starts 15 min AFTER the previous one finishes.
    // Prevents overlap if a slow network causes a cycle to exceed 15 min.
    @Scheduled(fixedDelayString = "${gdelt.collect-interval-ms:900000}")
    public void collectLatestNews() {
        log.info("Scheduled GDELT collection triggered");
        newsIngestionService.ingestLatest();
    }
}
