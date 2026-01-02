/* (C)2025 */
package com.rjain.spring_demo.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.rjain.spring_demo.util.JsonObjectMapperUtil;

@EnableCaching
@Configuration
public class RedisCacheConfig {

    @Profile("!test")
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // default TTL for all caches
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new GenericJacksonJsonRedisSerializer(
                                                JsonObjectMapperUtil.getObjectMapper())))
                        .disableCachingNullValues()
                        .entryTtl(Duration.ofMinutes(10)); // default TTL

        // per-cache TTL overrides
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(
                "helloCache", defaultConfig.entryTtl(Duration.ofSeconds(10))); // TTL for helloCache

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .enableStatistics()
                .build();
    }
}
