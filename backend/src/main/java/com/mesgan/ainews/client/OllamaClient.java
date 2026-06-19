package com.mesgan.ainews.client;

import com.mesgan.ainews.config.OllamaProperties;
import com.mesgan.ainews.dto.OllamaGenerateRequest;
import com.mesgan.ainews.dto.OllamaGenerateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaClient {

    private static final int MAX_ATTEMPTS = 3;
    private static final long TIMEOUT_SECONDS = 90;

    private final WebClient webClient;
    private final OllamaProperties ollamaProperties;

    public Optional<String> generate(String prompt) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String result = callOllama(prompt);
                return Optional.ofNullable(result);

            } catch (Exception e) {
                log.warn("Ollama attempt {}/{} failed: {}", attempt, MAX_ATTEMPTS, e.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    backoff(attempt);
                }
            }
        }
        log.error("Ollama failed after {} attempts", MAX_ATTEMPTS);
        return Optional.empty();
    }

    private String callOllama(String prompt) {
        OllamaGenerateRequest request = new OllamaGenerateRequest(
                ollamaProperties.getModel(),
                prompt,
                false  // stream=false: receive full response at once
        );

        OllamaGenerateResponse response = webClient.post()
                .uri(ollamaProperties.getBaseUrl() + "/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaGenerateResponse.class)
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .block();

        return response != null ? response.response() : null;
    }

    // Exponential backoff: 1s after attempt 1, 2s after attempt 2
    private void backoff(int attempt) {
        try {
            Thread.sleep(1000L * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
