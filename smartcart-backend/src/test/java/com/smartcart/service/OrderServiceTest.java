package com.smartcart.service;

import com.smartcart.dto.CreateOrderRequest;
import com.smartcart.dto.OrderDto;
import com.smartcart.entity.*;
import com.smartcart.enums.OrderStatus;
import com.smartcart.enums.PaymentMethod;
import com.smartcart.enums.PaymentStatus;
import com.smartcart.exception.BadRequestException;
import com.smartcart.exception.ResourceNotFoundException;
import com.smartcart.repository.*;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
@SuppressWarnings("null")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Address address;
    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private Order order;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@example.com").firstName("John").lastName("Doe").build();
        address = Address.builder().id(1L).street("123 Main St").city("New York").state("NY").zipCode("10001").country("USA").user(user).build();
        product = Product.builder().id(1L).name("Gadget").price(new BigDecimal("100.00")).stock(10).active(true).build();
        
        cartItem = CartItem.builder().id(1L).product(product).quantity(2).unitPrice(new BigDecimal("100.00")).build();
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(cartItem);

        cart = Cart.builder().id(1L).user(user).items(cartItems).build();

        order = Order.builder()
                .id(1L)
                .orderNumber("SC-12345")
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingAddress("123 Main St")
                .totalAmount(new BigDecimal("200.00"))
                .items(new ArrayList<>())
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .addressId(1L)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .build();
    }

    @Nested
    @DisplayName("Place Order Tests")
    class PlaceOrderTests {

        @Test
        @DisplayName("Should place order successfully when valid request is given")
        void placeOrder_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(address));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            OrderDto result = orderService.placeOrder(1L, createOrderRequest);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
            verify(orderRepository).save(any(Order.class));
            assertTrue(cart.getItems().isEmpty());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when address ID is invalid")
        void placeOrder_AddressNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> orderService.placeOrder(1L, createOrderRequest));
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException if stock is insufficient")
        void placeOrder_InsufficientStock() {
            product.setStock(1); // Set stock to less than cart quantity (2)
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(address));

            assertThrows(BadRequestException.class, () -> orderService.placeOrder(1L, createOrderRequest));
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order and restore product stock successfully")
        void cancelOrder_Success() {
            order.setStatus(OrderStatus.CONFIRMED);
            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(2)
                    .unitPrice(new BigDecimal("100.00"))
                    .build();
            order.getItems().add(item);
            
            when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);

            OrderDto result = orderService.cancelUserOrder(1L, 1L);

            assertNotNull(result);
            assertEquals(OrderStatus.CANCELLED, result.getStatus());
            assertEquals(12, product.getStock()); // Restored stock: 10 + 2
            verify(productRepository).save(product);
        }

        @Test
        @DisplayName("Should throw BadRequestException if order is already shipped")
        void cancelOrder_ShippedOrder() {
            order.setStatus(OrderStatus.SHIPPED);
            when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));

            assertThrows(BadRequestException.class, () -> orderService.cancelUserOrder(1L, 1L));
            verify(productRepository, never()).save(any(Product.class));
        }
    }
}
