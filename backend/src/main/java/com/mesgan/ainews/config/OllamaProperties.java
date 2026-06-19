package com.mesgan.ainews.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ollama")
@Data
public class OllamaProperties {

    private String baseUrl = "http://localhost:11434";
    private String model = "llama3.1";
}
