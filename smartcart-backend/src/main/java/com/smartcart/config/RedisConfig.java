package com.smartcart.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

// Redis + fallback to in-memory if Redis is down
@Slf4j
@Configuration
public class RedisConfig implements CachingConfigurer {

    private GenericJackson2JsonRedisSerializer redisJsonSerializer() {
        return GenericJackson2JsonRedisSerializer.builder()
                .defaultTyping(true)
                .build()
                .configure(objectMapper -> objectMapper.registerModule(new JavaTimeModule()));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisSerializer<Object> jsonSerializer = redisJsonSerializer();
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        try {
            // Verify Redis is actually reachable before building the cache manager
            connectionFactory.getConnection().ping();
            log.info("Redis connection verified. Using Redis as cache provider.");

            RedisSerializer<Object> jsonSerializer = redisJsonSerializer();
            RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
                    .serializeKeysWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair
                            .fromSerializer(jsonSerializer))
                    .disableCachingNullValues();

            // Per-cache TTL overrides
            Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
            cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofMinutes(10)));
            cacheConfigurations.put("product-detail", defaultConfig.entryTtl(Duration.ofMinutes(15)));
            cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofMinutes(30)));

            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(defaultConfig)
                    .withInitialCacheConfigurations(cacheConfigurations)
                    .transactionAware()
                    .build();
        } catch (Exception e) {
            log.warn("Redis is unavailable ({}). Falling back to in-memory cache. "
                    + "The application will work normally but cache will not persist across restarts.",
                    e.getMessage());
            return new ConcurrentMapCacheManager("products", "product-detail", "categories");
        }
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error("Redis GET failed for cache {}: {}", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error("Redis PUT failed for cache {}: {}", cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                // not much we can do here
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                // same, just let it go
            }
        };
    }
}
