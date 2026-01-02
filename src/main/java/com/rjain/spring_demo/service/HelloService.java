/* (C)2025 */
package com.rjain.spring_demo.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HelloService {

    @Cacheable(cacheNames = "helloCache", keyGenerator = "sanitisedKeyGenerator")
    public @NotNull String hello(String name) {
        log.info("Generating greeting for name: {}", name);
        if (StringUtils.isEmpty(name)) {
            return "Hello, World!";
        }
        return "Hello, " + StringUtils.normalizeSpace(name) + "!";
    }
}
