package com.aguinaldo.reliabilityapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorController {

    @GetMapping("/error-test")
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorTest() {
        return "Simulated internal server error";
    }
}
