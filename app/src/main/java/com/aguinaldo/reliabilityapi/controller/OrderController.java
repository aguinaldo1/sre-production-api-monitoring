package com.aguinaldo.reliabilityapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    @GetMapping("/orders")
    public List<Map<String, Object>> orders() {
        return List.of(
                Map.of(
                        "id", 1001,
                        "customer", "Customer A",
                        "status", "PROCESSING",
                        "total", 4850.00
                ),
                Map.of(
                        "id", 1002,
                        "customer", "Customer B",
                        "status", "COMPLETED",
                        "total", 1200.00
                ),
                Map.of(
                        "id", 1003,
                        "customer", "Customer C",
                        "status", "PENDING",
                        "total", 350.00
                )
        );
    }
}
