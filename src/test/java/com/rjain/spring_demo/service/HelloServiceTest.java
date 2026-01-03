/* (C)2026 */
package com.rjain.spring_demo.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("HelloService Tests")
class HelloServiceTest {

    @Autowired private HelloService helloService;

    @Autowired CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache("helloCache")).clear();
    }

    @AfterEach
    void tearDown() {}

    @Test
    @DisplayName("should return 'Hello, World!' when name is null")
    void testHelloWithNullName() {
        String result = helloService.hello(null);
        assertEquals("Hello, World!", result);
    }

    @Test
    @DisplayName("should return 'Hello, World!' when name is empty")
    void testHelloWithEmptyName() {
        String result = helloService.hello("");
        assertEquals("Hello, World!", result);
    }

    @Test
    @DisplayName("should return 'Hello, World!' when name is blank")
    void testHelloWithBlankName() {
        String result = helloService.hello("   ");
        assertEquals("Hello, World!", result);
    }

    @Test
    @DisplayName("should return greeting with normalized name")
    void testHelloWithValidName() {
        String result = helloService.hello("John");
        assertEquals("Hello, John!", result);
    }

    @Test
    @DisplayName("should normalize spaces in name")
    void testHelloWithMultipleSpaces() {
        String result = helloService.hello("  John   Doe  ");
        assertEquals("Hello, John Doe!", result);
    }

    @Test
    @DisplayName("should normalize tabs and newlines in name")
    void testHelloWithSpecialWhitespace() {
        String result = helloService.hello("John\t\nDoe");
        assertEquals("Hello, John Doe!", result);
    }

    @Test
    @DisplayName("should work with single character name")
    void testHelloWithSingleCharName() {
        String result = helloService.hello("A");
        assertEquals("Hello, A!", result);
    }

    @Test
    @DisplayName("should work with long name")
    void testHelloWithLongName() {
        String result = helloService.hello("Christopher Alexander");
        assertEquals("Hello, Christopher Alexander!", result);
    }

    @Test
    @DisplayName("should use cache on subsequent calls")
    void testHelloCaching() {
        String result1 = helloService.hello("Alice");
        long startTime = System.currentTimeMillis();
        String result2 = helloService.hello("Alice");
        long endTime = System.currentTimeMillis();

        assertEquals(result1, result2);
        assertEquals("Hello, Alice!", result2);
        // Cached call should be much faster than 100ms sleep
        assertTrue((endTime - startTime) < 100);
    }

    @Test
    @DisplayName("should return non-null greeting")
    void testHelloReturnsNonNull() {
        String result = helloService.hello("Test");
        assertNotNull(result);
    }

    @Test
    @DisplayName("should format greeting correctly")
    void testHelloFormatting() {
        String result = helloService.hello("Bob");
        assertTrue(result.startsWith("Hello, "));
        assertTrue(result.endsWith("!"));
        assertEquals("Hello, Bob!", result);
    }
}
