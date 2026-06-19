package com.mesgan.ainews.scheduler;

import com.mesgan.ainews.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiProcessorScheduler {

    private static final int BATCH_SIZE = 10;

    private final SummaryService summaryService;

    // Runs 2 minutes after the previous cycle completes.
    // fixedDelay prevents overlap if Ollama is slow (10 articles × 60s = 10 min possible).
    @Scheduled(fixedDelay = 120_000)
    public void processUnprocessedArticles() {
        log.info("AI processing cycle triggered");
        summaryService.processUnprocessedArticles(BATCH_SIZE);
    }
}
