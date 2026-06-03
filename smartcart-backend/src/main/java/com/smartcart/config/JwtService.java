package com.smartcart.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String DEFAULT_SECRET = "NGE2NzQ3M2QwNTk4YTJmZDJmZDAzNTEzM2QxYzRlOTc2OGEzNWVlMzEzYzBmNDQzODMxODdhNTUzODM1YmU1MQ==";

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private final Environment environment;

    public JwtService(Environment environment) {
        this.environment = environment;
    }

    /**
     * Validates that the JWT secret has been explicitly set in production.
     * Prevents running production with the publicly visible default key.
     */
    @PostConstruct
    public void validateJwtSecret() {
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (isProd && (secretKey == null || secretKey.isBlank() || secretKey.equals(DEFAULT_SECRET))) {
            throw new IllegalStateException(
                "FATAL: JWT_SECRET environment variable is not set or is still the default value. " +
                "Production deployments MUST use a unique, securely generated secret. " +
                "Generate one with: openssl rand -base64 64"
            );
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
            if (keyBytes.length < 32) {
                // If Base64 decoded key is too short, fallback to raw bytes
                keyBytes = secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException e) {
            // Fallback if not valid Base64
            keyBytes = secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        // Ensure key is at least 256 bits (32 bytes) for HS256
        if (keyBytes.length < 32) {
            try {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(keyBytes);
            } catch (java.security.NoSuchAlgorithmException ex) {
                throw new IllegalStateException("SHA-256 algorithm not available", ex);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
