package com.smartcart.service;

import com.smartcart.dto.PagedResponse;
import com.smartcart.dto.ProductDto;
import com.smartcart.entity.Category;
import com.smartcart.entity.Product;
import com.smartcart.exception.ResourceNotFoundException;
import com.smartcart.repository.CategoryRepository;
import com.smartcart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Electronics").build();
        product = Product.builder()
                .id(1L)
                .name("Smartphone")
                .description("Flagship smartphone")
                .price(new BigDecimal("999.99"))
                .discountPrice(new BigDecimal("899.99"))
                .stock(50)
                .brand("BrandX")
                .rating(4.5)
                .reviewCount(10)
                .active(true)
                .category(category)
                .build();

        productDto = ProductDto.builder()
                .name("Smartphone")
                .description("Flagship smartphone")
                .price(new BigDecimal("999.99"))
                .discountPrice(new BigDecimal("899.99"))
                .stock(50)
                .brand("BrandX")
                .categoryId(1L)
                .build();
    }

    @Nested
    @DisplayName("Get Products Tests")
    class GetProductsTests {

        @Test
        @DisplayName("Should return all active products paged")
        void getAllProducts_Success() {
            Page<Product> productPage = new PageImpl<>(List.of(product));
            when(productRepository.findByActiveTrue(any(Pageable.class))).thenReturn(productPage);

            PagedResponse<ProductDto> response = productService.getAllProducts(0, 10, "id", "asc");

            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("Smartphone", response.getContent().get(0).getName());
            verify(productRepository).findByActiveTrue(any(Pageable.class));
        }

        @Test
        @DisplayName("Should return product by ID successfully")
        void getProductById_Success() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            ProductDto result = productService.getProductById(1L);

            assertNotNull(result);
            assertEquals("Smartphone", result.getName());
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product ID is invalid")
        void getProductById_NotFound() {
            when(productRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(2L));
        }

        @Test
        @DisplayName("Should return products paged by category successfully")
        void getProductsByCategory_Success() {
            Page<Product> productPage = new PageImpl<>(List.of(product));
            when(productRepository.findByCategoryIdAndActiveTrue(eq(1L), any(Pageable.class))).thenReturn(productPage);

            PagedResponse<ProductDto> response = productService.getProductsByCategory(1L, 0, 10);

            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("Smartphone", response.getContent().get(0).getName());
        }
    }

    @Nested
    @DisplayName("Create/Update/Delete Product Tests")
    class ModifyProductTests {

        @Test
        @DisplayName("Should successfully create a product")
        void createProduct_Success() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(productRepository.save(any(Product.class))).thenReturn(product);

            ProductDto result = productService.createProduct(productDto);

            assertNotNull(result);
            assertEquals("Smartphone", result.getName());
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException during product creation when category not found")
        void createProduct_CategoryNotFound() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(productDto));
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("Should successfully update product details")
        void updateProduct_Success() {
            ProductDto updateDto = ProductDto.builder().name("Updated Smartphone").price(new BigDecimal("1099.99")).build();
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ProductDto result = productService.updateProduct(1L, updateDto);

            assertNotNull(result);
            assertEquals("Updated Smartphone", result.getName());
            assertEquals(new BigDecimal("1099.99"), result.getPrice());
        }

        @Test
        @DisplayName("Should successfully soft-delete a product")
        void deleteProduct_Success() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            productService.deleteProduct(1L);

            assertFalse(product.getActive());
            verify(productRepository).save(product);
        }
    }
}
