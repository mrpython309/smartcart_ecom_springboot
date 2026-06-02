package com.smartcart.service;

import com.smartcart.dto.CartDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
@SuppressWarnings("null")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@example.com").build();
        product = Product.builder()
                .id(1L)
                .name("Premium Product")
                .price(new BigDecimal("100.00"))
                .discountPrice(new BigDecimal("90.00"))
                .stock(10)
                .active(true)
                .build();

        cart = Cart.builder()
                .id(1L)
                .user(user)
                .items(new ArrayList<>())
                .build();

        cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("90.00"))
                .build();
    }

    @Nested
    @DisplayName("Get Cart Tests")
    class GetCartTests {

        @Test
        @DisplayName("Should successfully retrieve user's cart")
        void getCart_Success() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

            CartDto result = cartService.getCart(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(cartRepository).findByUserId(1L);
        }

        @Test
        @DisplayName("Should create a new cart if one does not exist for the user")
        void getCart_CreateNew_WhenNotExist() {
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CartDto result = cartService.getCart(1L);

            assertNotNull(result);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("Add to Cart Tests")
    class AddToCartTests {

        @Test
        @DisplayName("Should successfully add a product to the cart")
        void addToCart_Success() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(cartItemRepository.findByCartIdAndProductId(any(), any())).thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

            CartDto result = cartService.addToCart(1L, 1L, 2);

            assertNotNull(result);
            verify(cartItemRepository).save(any(CartItem.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException if product is inactive")
        void addToCart_InactiveProduct() {
            product.setActive(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            assertThrows(BadRequestException.class, () -> cartService.addToCart(1L, 1L, 2));
        }

        @Test
        @DisplayName("Should throw BadRequestException if requested quantity exceeds stock")
        void addToCart_InsufficientStock() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(product));

            assertThrows(BadRequestException.class, () -> cartService.addToCart(1L, 1L, 15));
        }
    }
}
