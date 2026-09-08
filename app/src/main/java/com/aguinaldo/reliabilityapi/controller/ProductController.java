package com.aguinaldo.reliabilityapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ProductController {

    @GetMapping("/products")
    public List<Map<String, Object>> products() {
        return List.of(
                Map.of(
                        "id", 1,
                        "name", "Notebook",
                        "price", 4500.00
                ),
                Map.of(
                        "id", 2,
                        "name", "Monitor",
                        "price", 1200.00
                ),
                Map.of(
                        "id", 3,
                        "name", "Keyboard",
                        "price", 350.00
                )
        );
    }
}
