package com.smartcart.service;

import com.smartcart.dto.ProductDto;
import com.smartcart.dto.SmartSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiSearchService {

    private final ChatClient chatClient;

    public AiSearchService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public SmartSearchCriteria parseQuery(String query) {
        try {
            var outputConverter = new BeanOutputConverter<>(SmartSearchCriteria.class);
            String format = outputConverter.getFormat();

            String prompt = String.format(
                    "You are an AI for an e-commerce platform. Extract search parameters from this query: '%s'. " +
                    "Fields: 'category' (string or null, ONLY use broad departments like 'Electronics', 'Fashion', 'Books'. For specific items like 'laptop' or 'shoes', set category to null and put them in keywords), " +
                    "'minPrice' (number or null), 'maxPrice' (number or null), 'keywords' (array of string keywords, include the specific product name here). " +
                    "%%s", query, format);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return outputConverter.convert(response);
        } catch (Exception e) {
            log.error("Failed to parse AI query using Spring AI: {}", e.getMessage());
            return SmartSearchCriteria.builder().keywords(List.of(query.split("\\s+"))).build();
        }
    }

    public String generateRecommendation(String userQuery, List<ProductDto> products) {
        if (products == null || products.isEmpty()) {
            return "I couldn't find any products matching your search. Try adjusting your price limit or keywords!";
        }

        String productDetails = products.stream()
                .limit(3) // Feed top 3 products to keep it concise
                .map(p -> String.format("- %s (Brand: %s, Price: %.2f)", p.getName(), p.getBrand(), p.getEffectivePrice().doubleValue()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format(
                "A user searched for: '%s'. We found these products in our database:\n%s\n" +
                "Write a short, friendly, and enthusiastic 1-2 sentence response recommending these products to the user. " +
                "Do not use markdown formatting. Be concise.", 
                userQuery, productDetails);

        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to generate AI recommendation: {}", e.getMessage());
            return "Here are the best products I found for you!";
        }
    }
}
