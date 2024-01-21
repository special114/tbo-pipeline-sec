package com.example.thymeleaf.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VulnerableController {

    @GetMapping("/lul")
    public List<String> getClients() {
        return List.of("clinet1");
    }
}
