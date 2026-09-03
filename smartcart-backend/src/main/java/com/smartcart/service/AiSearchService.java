package com.smartcart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.dto.SmartSearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSearchService {

    @Value("${app.gemini.api-key}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    public SmartSearchCriteria parseQuery(String query) {
        if (geminiApiKey == null || geminiApiKey.contains("YOUR_GEMINI_API_KEY")) {
            log.warn("Gemini API Key is missing. Falling back to keyword search.");
            return SmartSearchCriteria.builder().keywords(List.of(query.split("\\s+"))).build();
        }

        String prompt = "You are an AI for an e-commerce platform. Extract search parameters from this query: \"" + query + "\". " +
                "Return ONLY a valid JSON object (no markdown, no backticks, just the raw JSON). " +
                "Fields: 'category' (string or null), 'minPrice' (number or null), 'maxPrice' (number or null), 'keywords' (array of string keywords). " +
                "Example output: {\"category\":\"electronics\",\"minPrice\":null,\"maxPrice\":500,\"keywords\":[\"budget\",\"smartphone\"]}";

        try {
            Map<String, Object> requestBody = new HashMap<>();
            
            // Re-constructing the nested map correctly
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_API_URL + geminiApiKey, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                    String jsonString = (String) parts.get(0).get("text");
                    
                    // Clean up jsonString if the model returned markdown
                    jsonString = jsonString.replaceAll("```json", "").replaceAll("```", "").trim();

                    return objectMapper.readValue(jsonString, SmartSearchCriteria.class);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse AI query: {}", e.getMessage());
        }

        // Fallback to simple keyword search
        return SmartSearchCriteria.builder().keywords(List.of(query.split("\\s+"))).build();
    }
}
