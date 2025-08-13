package com.jjas.similar_products.infrastructure.http.exception;

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
