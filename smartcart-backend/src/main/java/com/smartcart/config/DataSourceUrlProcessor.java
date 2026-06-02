package com.smartcart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * DataSourceUrlProcessor automatically parses standard cloud database connection URLs 
 * (e.g. postgres://username:password@host:port/database) and reconfigures the 
 * DataSourceProperties dynamically into the format expected by JDBC.
 */
@Slf4j
@Component
public class DataSourceUrlProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(@org.springframework.lang.NonNull Object bean, @org.springframework.lang.NonNull String beanName) throws BeansException {
        if (bean instanceof DataSourceProperties) {
            DataSourceProperties properties = (DataSourceProperties) bean;
            String url = properties.getUrl();

            if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://") || url.startsWith("mysql://"))) {
                log.info("Detected cloud-style database connection URL: {}", maskUrl(url));
                try {
                    // Temporarily treat as http schema for URI parsing to handle user-info correctly
                    String tempUrl;
                    if (url.startsWith("postgres://")) {
                        tempUrl = url.replace("postgres://", "http://");
                    } else if (url.startsWith("postgresql://")) {
                        tempUrl = url.replace("postgresql://", "http://");
                    } else {
                        tempUrl = url.replace("mysql://", "http://");
                    }

                    URI uri = URI.create(tempUrl);
                    String host = uri.getHost();
                    int port = uri.getPort();
                    String path = uri.getPath();
                    String userInfo = uri.getUserInfo();

                    String jdbcPrefix = url.startsWith("mysql://") ? "jdbc:mysql://" : "jdbc:postgresql://";
                    String jdbcUrl = jdbcPrefix + host + (port != -1 ? ":" + port : "") + path;

                    // Preserve existing query parameters if any (like SSL settings, timezone etc.)
                    String query = uri.getQuery();
                    if (query != null && !query.isEmpty()) {
                        jdbcUrl += "?" + query;
                    }

                    properties.setUrl(jdbcUrl);
                    log.info("Successfully converted database URL to JDBC standard format: {}", jdbcUrl);

                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        properties.setUsername(parts[0]);
                        properties.setPassword(parts[1]);
                        log.info("Extracted database username '{}' from connection URL", parts[0]);
                    }
                } catch (Exception e) {
                    log.error("Failed to parse cloud-style database URL: {}. Attempting fallback conversion...", e.getMessage());
                    // Simple fallback replacement if URI parsing fails
                    if (url.startsWith("postgres://")) {
                        properties.setUrl(url.replace("postgres://", "jdbc:postgresql://"));
                    } else if (url.startsWith("postgresql://")) {
                        properties.setUrl(url.replace("postgresql://", "jdbc:postgresql://"));
                    } else if (url.startsWith("mysql://")) {
                        properties.setUrl(url.replace("mysql://", "jdbc:mysql://"));
                    }
                }
            }
        }
        return bean;
    }

    private String maskUrl(String url) {
        if (url == null) return null;
        // Hide password in logs
        return url.replaceAll("(?<=://[^:]*:)[^@]*", "*****");
    }
}
