package com.jjas.similar_products.infrastructure.http.exception;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String msg) {
        super(msg);
    }
}
