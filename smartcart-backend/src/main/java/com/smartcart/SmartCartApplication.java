package com.smartcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(excludeName = "org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration")
@EnableScheduling
@EnableCaching
public class SmartCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCartApplication.class, args);
    }
}
