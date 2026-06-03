package com.smartcart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Profile("dev")
public class DebugController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/db-connectivity")
    public String testDb() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return "Database connection successful! Result: " + result;
        } catch (Exception e) {
            return "Database connectivity FAILED: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }
}
