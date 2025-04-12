package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.gemini.GeminiRequest;
import org.example.dto.gemini.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
@Service
@Slf4j
public class GeminiService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    public String generateContent(String systemPrompt, String userPrompt) {
        // 合併 systemPrompt 和 userPrompt
        String combinedPrompt = systemPrompt + "\n\n" + userPrompt;
        GeminiRequest request = new GeminiRequest(
                List.of(
                        new GeminiRequest.Content(
                                "user",
                                List.of(new GeminiRequest.Content.Part(combinedPrompt))
                        )
                ),
                new GeminiRequest.GenerationConfig() // 預設配置
        );

        Mono<GeminiResponse> responseMono = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/gemini-1.5-pro:generateContent")
                        .queryParam("key", apiKey)
                        .build())
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.isError(), response -> response.bodyToMono(String.class)
                        .map(errorBody -> {
                            log.error("Gemini API 錯誤: {}", errorBody);
                            throw new RuntimeException("API 呼叫失敗: " + errorBody);
                        }))
                .bodyToMono(GeminiResponse.class);

        try {
            GeminiResponse response = responseMono.block();
            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                return response.getCandidates().get(0).getContent().getParts().get(0).getText();
            }
            log.error("無法從 Gemini API 獲取回應，userPrompt: {}", userPrompt);
            return "抱歉，我無法生成回應，請稍後再試。";
        } catch (Exception e) {
            log.error("Gemini API 呼叫異常: {}", e.getMessage());
            return "抱歉，我無法生成回應，請稍後再試。";
        }
    }
}