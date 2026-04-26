package com.smartcart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be at least 0")
    private BigDecimal price;

    @Min(value = 0, message = "Discount price must be zero or positive")
    private BigDecimal discountPrice;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be zero or positive")
    private Integer stock;

    private String imageUrl;
    private String brand;
    private Double rating;
    private Integer reviewCount;
    private Boolean active;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
    private String categoryName;

    private Integer discountPercentage;
    private BigDecimal effectivePrice;
    private LocalDateTime createdAt;
}
