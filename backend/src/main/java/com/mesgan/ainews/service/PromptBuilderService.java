package com.mesgan.ainews.service;

import com.mesgan.ainews.entity.Article;
import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    private static final String CATEGORY_OPTIONS =
            "AI & Machine Learning, Technology & Software Engineering, Economy & Business, " +
            "Cybersecurity, Global News & Politics, Health & Medicine, " +
            "Environment & Climate, Society & Culture";

    public String buildSummaryPrompt(Article article) {
        String sourceName = article.getSourceName() != null ? article.getSourceName() : "Unknown";
        String category   = article.getCategory()   != null ? article.getCategory()   : "General";

        return """
                You are an expert AI news analyst. Analyze this news article using the metadata below.

                Article URL: %s
                News Source: %s
                Detected Category: %s

                Respond with ONLY a valid JSON object. No explanation, no markdown, no text before or after the JSON.

                Use exactly this structure:
                {
                  "title": "A clear, factual headline for this article (max 120 characters)",
                  "oneSentenceSummary": "One concise sentence describing the key development",
                  "keyPoints": "• First key point\\n• Second key point\\n• Third key point",
                  "whyItMatters": "Two to three sentences explaining the broader significance of this story",
                  "aiCategory": "Pick exactly one: %s",
                  "importanceScore": 70
                }

                Rules:
                - importanceScore is an integer from 1 to 100. Use the full range. Reserve 90-100 for major global events.
                - aiCategory must be exactly one of the options listed, copied word for word.
                - Base all analysis on the URL path, source reputation, and detected category.
                """.formatted(article.getUrl(), sourceName, category, CATEGORY_OPTIONS);
    }
}
