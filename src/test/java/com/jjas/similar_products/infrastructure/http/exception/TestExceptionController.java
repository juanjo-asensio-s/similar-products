package com.jjas.similar_products.infrastructure.http.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.MethodNotAllowedException;

@RestController
class TestExceptionController {

    @GetMapping("/test/not-found")
    public void notFound() {
        throw new ProductNotFoundException("123");
    }

    @GetMapping("/test/invalid-input")
    public void invalidInput() {
        throw new InvalidInputException("Invalid input provided");
    }

    @GetMapping("/test/upstream-failure")
    public void upstreamFailure() {
        throw new ExternalServiceException("External service error", null);
    }

    @PostMapping("/test/method-not-allowed")
    public void methodNotAllowed() {
        throw new MethodNotAllowedException("Method not allowed Error", null);
    }

    @GetMapping("/test/generic-error")
    public void genericError() {
        throw new RuntimeException("Some unexpected failure");
    }
}
