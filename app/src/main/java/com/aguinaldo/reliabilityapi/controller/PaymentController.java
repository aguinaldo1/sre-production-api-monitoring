package com.aguinaldo.reliabilityapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PaymentController {

    @GetMapping("/payments")
    public List<Map<String, Object>> payments() {
        return List.of(
                Map.of(
                        "id", 5001,
                        "orderId", 1001,
                        "status", "PENDING",
                        "amount", 4850.00
                ),
                Map.of(
                        "id", 5002,
                        "orderId", 1002,
                        "status", "APPROVED",
                        "amount", 1200.00
                ),
                Map.of(
                        "id", 5003,
                        "orderId", 1003,
                        "status", "DECLINED",
                        "amount", 350.00
                )
        );
    }
}
