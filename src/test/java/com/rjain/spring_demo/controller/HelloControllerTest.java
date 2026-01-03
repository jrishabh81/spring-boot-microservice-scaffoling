/* (C)2026 */
package com.rjain.spring_demo.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.rjain.spring_demo.service.HelloService;

@ActiveProfiles("test")
@WebMvcTest(controllers = HelloController.class)
class HelloControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private HelloService helloService;

    @Test
    void hello_withName_returnsGreeting() throws Exception {
        when(helloService.hello(eq("Alice"))).thenReturn("Hello, Alice!");

        mockMvc.perform(get("/hello").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Alice!"));

        verify(helloService).hello(eq("Alice"));
    }

    @Test
    void hello_withoutName_returnsDefaultGreeting() throws Exception {
        when(helloService.hello(isNull())).thenReturn("Hello, World!");

        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));

        verify(helloService).hello(isNull());
    }
}
