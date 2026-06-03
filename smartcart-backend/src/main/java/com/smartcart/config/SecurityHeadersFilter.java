package com.smartcart.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds production-grade HTTP security headers to all responses.
 *
 * <p>These headers protect against common web vulnerabilities:
 * <ul>
 *   <li><b>X-Content-Type-Options</b> — prevents MIME-type sniffing attacks</li>
 *   <li><b>X-Frame-Options</b> — prevents clickjacking by disallowing framing</li>
 *   <li><b>X-XSS-Protection</b> — enables browser-level XSS filtering</li>
 *   <li><b>Strict-Transport-Security</b> — enforces HTTPS for 1 year</li>
 *   <li><b>Referrer-Policy</b> — limits referrer information leakage</li>
 *   <li><b>Permissions-Policy</b> — restricts browser feature access</li>
 *   <li><b>Cache-Control</b> — prevents caching of API responses</li>
 * </ul>
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Prevent MIME-type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Enable browser XSS protection
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Enforce HTTPS (1 year, including subdomains)
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Limit referrer information leakage
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Restrict browser feature access
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        // Prevent caching of API responses (static assets are cached by nginx/CDN)
        if (request.getRequestURI().startsWith("/api/")) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        filterChain.doFilter(request, response);
    }
}
