package com.smartcart.controller;

import com.smartcart.dto.ApiResponse;
import com.smartcart.dto.PagedResponse;
import com.smartcart.dto.ProductDto;
import com.smartcart.dto.SmartSearchCriteria;
import com.smartcart.service.AiSearchService;
import com.smartcart.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Features", description = "AI Shopping Assistant Endpoints")
public class AiController {

    private final AiSearchService aiSearchService;
    private final ProductService productService;

    @GetMapping("/search")
    @Operation(summary = "Search products using Natural Language (AI)")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> searchProductsByAi(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        // 1. Send natural language to Gemini AI to get structured criteria
        SmartSearchCriteria criteria = aiSearchService.parseQuery(query);
        
        // 2. Query the database dynamically based on AI criteria
        PagedResponse<ProductDto> products = productService.searchByAi(criteria, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
