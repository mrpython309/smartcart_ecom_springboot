package com.smartcart.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

// one-time fix: some products had null version from before I added @Version
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile({"dev", "prod"})
public class DataMigrationConfig {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner initializeProductVersions() {
        return args -> {
            log.info("Checking for products with null version numbers...");
            try {
                // Update all products where version is null to 0
                // We use native SQL to bypass Hibernate's optimistic locking checks during the fix
                int updatedCount = jdbcTemplate.update(
                        "UPDATE products SET version = 0 WHERE version IS NULL"
                );
                
                if (updatedCount > 0) {
                    log.info("Successfully initialized version numbers for {} products.", updatedCount);
                } else {
                    log.info("No products with null version numbers found.");
                }
            } catch (Exception e) {
                log.error("Failed to initialize product versions: {}", e.getMessage());
            }
        };
    }
}
