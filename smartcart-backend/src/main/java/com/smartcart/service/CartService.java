package com.smartcart.service;

import com.smartcart.dto.CartDto;
import com.smartcart.dto.CartItemDto;
import com.smartcart.entity.Cart;
import com.smartcart.entity.CartItem;
import com.smartcart.entity.Product;
import com.smartcart.entity.User;
import com.smartcart.exception.BadRequestException;
import com.smartcart.exception.ResourceNotFoundException;
import com.smartcart.repository.CartItemRepository;
import com.smartcart.repository.CartRepository;
import com.smartcart.repository.ProductRepository;
import com.smartcart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartDto getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToCartDto(cart);
    }

    @Transactional
    public CartDto addToCart(Long userId, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.getActive()) {
            throw new BadRequestException("Product is not available");
        }

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        if (product.getStock() < quantity) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }

        Cart cart = getOrCreateCart(userId);

        // Check if product already in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setUnitPrice(product.getEffectivePrice());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getEffectivePrice())
                    .build();
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        cart = cartRepository.findByUserId(userId).orElse(cart);
        log.info("Product {} added to cart for user {}", productId, userId);
        return mapToCartDto(cart);
    }

    @Transactional
    public CartDto updateCartItem(Long userId, Long cartItemId, int quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", cartItemId));

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStock() < quantity) {
                throw new BadRequestException("Insufficient stock. Available: " + item.getProduct().getStock());
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        cart = cartRepository.findByUserId(userId).orElse(cart);
        return mapToCartDto(cart);
    }

    @Transactional
    public CartDto removeFromCart(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", "id", cartItemId));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        cart = cartRepository.findByUserId(userId).orElse(cart);
        log.info("Item removed from cart: {}", cartItemId);
        return mapToCartDto(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Cart cleared for user: {}", userId);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            Cart newCart = Cart.builder()
                    .user(user)
                    .items(new ArrayList<>())
                    .build();
            return cartRepository.save(newCart);
        });
    }

    private CartDto mapToCartDto(Cart cart) {
        return CartDto.builder()
                .id(cart.getId())
                .items(cart.getItems().stream().map(this::mapToCartItemDto).collect(Collectors.toList()))
                .totalPrice(cart.getTotalPrice())
                .totalItems(cart.getTotalItems())
                .build();
    }

    private CartItemDto mapToCartItemDto(CartItem item) {
        Product product = item.getProduct();
        return CartItemDto.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getImageUrl())
                .productPrice(product.getPrice())
                .productDiscountPrice(product.getDiscountPrice())
                .productStock(product.getStock())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
