package com.smartcart.controller;

import com.smartcart.dto.PagedResponse;
import com.smartcart.dto.ProductDto;
import com.smartcart.service.FileStorageService;
import com.smartcart.config.JwtService;
import com.smartcart.config.JwtAuthFilter;
import com.smartcart.repository.UserRepository;
import com.smartcart.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProductController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false) // Disable security for simpler API slice testing
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @WithMockUser
    void getAllProducts_ShouldReturnOk() throws Exception {
        PagedResponse<ProductDto> pagedResponse = PagedResponse.<ProductDto>builder()
                .content(List.of(ProductDto.builder().id(1L).name("Test Product").build()))
                .page(0)
                .size(10)
                .totalElements(1)
                .build();

        when(productService.getAllProducts(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        ProductDto product = ProductDto.builder().id(1L).name("Special Product").build();
        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Special Product"));
    }
}
