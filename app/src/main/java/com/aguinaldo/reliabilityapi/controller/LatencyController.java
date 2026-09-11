package com.aguinaldo.reliabilityapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LatencyController {

    @GetMapping("/slow")
    public Map<String, Object> slow() throws InterruptedException {

        Thread.sleep(1000);

        return Map.of(
                "status", "OK",
                "message", "Simulated latency",
                "delayMs", 1000
        );
    }
}
