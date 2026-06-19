package com.mesgan.ainews.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(config -> config.defaultCodecs()
                        .maxInMemorySize(50 * 1024 * 1024)) // 50MB for GDELT ZIP downloads
                .build();
    }
}
