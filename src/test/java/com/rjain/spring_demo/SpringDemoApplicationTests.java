/* (C)2025 */
package com.rjain.spring_demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class SpringDemoApplicationTests {

    @Test
    void contextLoads() {}
    //
    //    @Test
    //    void checkHealth() throws Exception {
    //        String response = restTemplateBuilder.getForObject("http://localhost:" + port +
    // "/actuator/health", String.class);
    //        assert response != null;
    //        assertTrue(response.contains("\"status\":\"UP\""));
    //
    //    }
}
