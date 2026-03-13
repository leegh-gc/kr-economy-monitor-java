package com.kreconomy.monitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    private static final String TEXT_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final String IMAGE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final String apiKey;
    private final String textModel;
    private final String imageModel;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiService(
            @Value("${app.gemini.api-key:}") String apiKey,
            @Value("${app.gemini.model-text}") String textModel,
            @Value("${app.gemini.model-image}") String imageModel,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.textModel = textModel;
        this.imageModel = imageModel;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Gemini text API로 분석 텍스트를 생성한다.
     */
    public String generateAnalysis(String prompt) {
        if (!isConfigured()) {
            return "GEMINI_API_KEY 환경 변수가 설정되지 않아 AI 분석을 사용할 수 없습니다.";
        }
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    )),
                    "generationConfig", Map.of("maxOutputTokens", 2048)
            );

            String url = String.format(TEXT_URL, textModel, apiKey);
            String responseBody = postJson(url, objectMapper.writeValueAsString(body));

            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText("분석 결과를 가져올 수 없습니다.");
        } catch (Exception e) {
            log.error("Gemini analysis failed: {}", e.getMessage());
            return "AI 분석 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * Gemini image API로 컷툰 이미지(base64)를 생성한다.
     */
    public String generateCartoon(String analysisText, String context) {
        if (!isConfigured()) return null;
        try {
            String summary = analysisText.length() > 500
                    ? analysisText.substring(0, 500)
                    : analysisText;
            String imagePrompt = String.format(
                    "다음 한국 %s 분석 내용을 바탕으로 신문 만화 스타일의 단일 컷 이미지를 생성해 주세요. " +
                    "한국어 텍스트 없이 그림만으로 표현해 주세요.\n\n분석 내용 요약:\n%s",
                    context, summary
            );

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", imagePrompt))
                    )),
                    "generationConfig", Map.of("responseModalities", List.of("IMAGE", "TEXT"))
            );

            String url = String.format(IMAGE_URL, imageModel, apiKey);
            String responseBody = postJson(url, objectMapper.writeValueAsString(body));

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode parts = root.path("candidates").get(0)
                    .path("content").path("parts");
            for (JsonNode part : parts) {
                JsonNode inlineData = part.path("inlineData");
                if (!inlineData.isMissingNode()) {
                    return inlineData.path("data").asText(null);
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("Gemini cartoon generation failed: {}", e.getMessage());
            return null;
        }
    }

    private String postJson(String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Gemini API error " + response.statusCode() + ": " + response.body());
        }
        return response.body();
    }
}
