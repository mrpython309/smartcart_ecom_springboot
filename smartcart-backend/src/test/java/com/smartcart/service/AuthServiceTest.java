package com.smartcart.service;

import com.smartcart.config.JwtService;
import com.smartcart.dto.AuthResponse;
import com.smartcart.dto.LoginRequest;
import com.smartcart.dto.RegisterRequest;
import com.smartcart.entity.User;
import com.smartcart.enums.Role;
import com.smartcart.exception.BadRequestException;
import com.smartcart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .phone("1234567890")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register a new user successfully")
        void register_Success() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

            AuthResponse response = authService.register(registerRequest);

            assertNotNull(response);
            assertEquals("jwt-token", response.getToken());
            assertNotNull(response.getUser());
            assertEquals("john@example.com", response.getUser().getEmail());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when email already exists")
        void register_EmailAlreadyExists() {
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should encode password before saving")
        void register_ShouldEncodePassword() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

            authService.register(registerRequest);

            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("Should assign USER role by default on registration")
        void register_ShouldAssignUserRole() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(1L);
                savedUser.setCreatedAt(LocalDateTime.now());
                return savedUser;
            });
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

            AuthResponse response = authService.register(registerRequest);

            assertEquals(Role.USER, response.getUser().getRole());
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_Success() {
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

            AuthResponse response = authService.login(loginRequest);

            assertNotNull(response);
            assertEquals("jwt-token", response.getToken());
            assertEquals("john@example.com", response.getUser().getEmail());
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid credentials")
        void login_InvalidCredentials() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        }

        @Test
        @DisplayName("Should throw BadRequestException when user not found after auth")
        void login_UserNotFoundAfterAuth() {
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

            assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
        }
    }
}
