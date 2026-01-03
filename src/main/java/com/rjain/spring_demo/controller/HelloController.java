/* (C)2025 */
package com.rjain.spring_demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rjain.spring_demo.service.HelloService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/hello")
public class HelloController {
    private final HelloService helloService;

    @GetMapping
    public ResponseEntity<String> hello(
            @RequestParam(name = "name", required = false) String name) {
        return ResponseEntity.ok(helloService.hello(name));
    }
}
