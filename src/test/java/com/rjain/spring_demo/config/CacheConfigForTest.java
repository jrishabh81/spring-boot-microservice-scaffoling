/* (C)2024 */
package com.rjain.spring_demo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
@EnableCaching
public class CacheConfigForTest {

    @Bean
    public CacheManager cacheManager() {
        return new org.springframework.cache.concurrent.ConcurrentMapCacheManager("helloCache");
    }
}
