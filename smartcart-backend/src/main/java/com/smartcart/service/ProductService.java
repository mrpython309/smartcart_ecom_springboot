package com.smartcart.service;

import com.smartcart.dto.PagedResponse;
import com.smartcart.dto.ProductDto;
import com.smartcart.entity.Category;
import com.smartcart.entity.Product;
import com.smartcart.exception.ResourceNotFoundException;
import com.smartcart.repository.CategoryRepository;
import com.smartcart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductService manages the product lifecycle.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'all_' + #page + '_' + #size + '_' + #sortBy + '_' + #sortDir")
    public PagedResponse<ProductDto> getAllProducts(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching products — page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByActiveTrue(pageable);
        return mapToPagedResponse(products);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "product-detail", key = "#id")
    public ProductDto getProductById(Long id) {
        log.info("Fetching product by id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToDto(product);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> searchProducts(String query, int page, int size) {
        log.info("Searching products — query='{}', page={}, size={}", query, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.searchProducts(query, pageable);
        return mapToPagedResponse(products);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> filterProducts(String query, Long categoryId,
                                                     BigDecimal minPrice, BigDecimal maxPrice,
                                                     int page, int size, String sortBy, String sortDir) {
        log.info("Filtering products — query='{}', category={}, minPrice={}, maxPrice={}", query, categoryId, minPrice, maxPrice);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter for active products
            predicates.add(cb.equal(root.get("active"), true));

            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate brandLike = cb.like(cb.lower(root.get("brand")), searchPattern);
                predicates.add(cb.or(nameLike, brandLike));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> products = productRepository.findAll(spec, pageable);
        return mapToPagedResponse(products);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'category_' + #categoryId + '_' + #page + '_' + #size")
    public PagedResponse<ProductDto> getProductsByCategory(Long categoryId, int page, int size) {
        log.info("Fetching products by category={}, page={}, size={}", categoryId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        return mapToPagedResponse(products);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true)
    })
    public ProductDto createProduct(ProductDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .discountPrice(dto.getDiscountPrice())
                .stock(dto.getStock())
                .imageUrl(dto.getImageUrl())
                .brand(dto.getBrand())
                .rating(dto.getRating() != null ? dto.getRating() : 0.0)
                .reviewCount(dto.getReviewCount() != null ? dto.getReviewCount() : 0)
                .active(true)
                .category(category)
                .build();

        product = productRepository.save(product);
        log.info("Product created: id={}, name='{}'", product.getId(), product.getName());
        return mapToDto(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product-detail", key = "#id")
    })
    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            product.setCategory(category);
        }

        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getDiscountPrice() != null) product.setDiscountPrice(dto.getDiscountPrice());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getImageUrl() != null) product.setImageUrl(dto.getImageUrl());
        if (dto.getBrand() != null) product.setBrand(dto.getBrand());
        if (dto.getActive() != null) product.setActive(dto.getActive());

        product = productRepository.save(product);
        log.info("Product updated: id={}, name='{}'", product.getId(), product.getName());
        return mapToDto(product);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "product-detail", key = "#id")
    })
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
        productRepository.save(product);
        log.warn("Product soft-deleted: id={}", id);
    }

    private ProductDto mapToDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .brand(product.getBrand())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .active(product.getActive())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .discountPercentage(product.getDiscountPercentage())
                .effectivePrice(product.getEffectivePrice())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private PagedResponse<ProductDto> mapToPagedResponse(Page<Product> page) {
        List<ProductDto> content = page.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return PagedResponse.<ProductDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
