package com.mesgan.ainews.client;

import com.mesgan.ainews.dto.GdeltArticleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipInputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class GdeltClient {

    private static final String LAST_UPDATE_URL =
            "http://data.gdeltproject.org/gdeltv2/lastupdate.txt";
    private static final DateTimeFormatter GDELT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final WebClient webClient;

    public List<GdeltArticleDto> fetchLatestArticles() {
        try {
            String lastUpdateContent = fetchLastUpdateFile();
            String gkgUrl = parseGkgUrl(lastUpdateContent);
            log.info("Downloading GKG file: {}", gkgUrl);

            byte[] zipBytes = downloadFile(gkgUrl);
            List<GdeltArticleDto> articles = parseGkgZip(zipBytes);
            log.info("Parsed {} articles from GDELT GKG", articles.size());
            return articles;

        } catch (Exception e) {
            log.error("GDELT fetch failed: {}", e.getMessage());
            return List.of();
        }
    }

    private String fetchLastUpdateFile() {
        return webClient.get()
                .uri(LAST_UPDATE_URL)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(15))
                .block();
    }

    // lastupdate.txt has 3 lines: export, mentions, gkg — each: "size hash url"
    private String parseGkgUrl(String content) {
        return Arrays.stream(content.split("\n"))
                .map(String::trim)
                .filter(line -> line.contains(".gkg.csv.zip"))
                .map(line -> line.split("\\s+"))
                .filter(parts -> parts.length == 3)
                .map(parts -> parts[2])
                .findFirst()
                .orElseThrow(() -> new RuntimeException("GKG URL not found in lastupdate.txt"));
    }

    private byte[] downloadFile(String url) {
        return webClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(60))
                .block();
    }

    private List<GdeltArticleDto> parseGkgZip(byte[] zipBytes) throws Exception {
        List<GdeltArticleDto> articles = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            if (zis.getNextEntry() != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(zis, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    GdeltArticleDto dto = parseLine(line);
                    if (dto != null) {
                        articles.add(dto);
                    }
                }
            }
        }

        return articles;
    }

    private GdeltArticleDto parseLine(String line) {
        try {
            // GKG v2 is tab-separated with 27 fields
            String[] fields = line.split("\t", -1);
            if (fields.length < 16) return null;

            String dateStr   = fields[1].trim();   // DATE: YYYYMMDDHHmmss
            String source    = fields[3].trim();    // SourceCommonName
            String url       = fields[4].trim();    // DocumentIdentifier
            String themes    = fields[8].trim();    // V2Themes
            String toneStr   = fields[15].trim();   // V21Tone (comma-separated)

            if (url.isBlank() || !url.startsWith("http")) return null;

            float tone = parseTone(toneStr);
            Instant publishedAt = parseDate(dateStr);

            return new GdeltArticleDto(url, source, themes, tone, publishedAt);

        } catch (Exception e) {
            return null; // silently skip malformed rows
        }
    }

    private float parseTone(String toneStr) {
        if (toneStr.isBlank()) return 0f;
        try {
            // format: tone,positive,negative,polarity,activityDensity,selfRef,wordCount
            return Float.parseFloat(toneStr.split(",")[0]);
        } catch (Exception e) {
            return 0f;
        }
    }

    private Instant parseDate(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr, GDELT_DATE_FORMAT).toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
