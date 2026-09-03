package com.smartcart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartSearchCriteria {
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> keywords;
}
