package com.jjas.similar_products.infrastructure.http.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /***
     * Enriches the ProblemDetail with common metadata (title, path, timestamp, traceId).
     *
     * @param pd   the ProblemDetail to enrich
     * @param req  the current HTTP request
     * @param code application-specific error code to set as the title
     */
    private static void enrichException(ProblemDetail pd, HttpServletRequest req, ErrorCode code) {
        pd.setTitle(code.name());
        pd.setInstance(java.net.URI.create(req.getRequestURI()));
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", java.time.OffsetDateTime.now());
        pd.setProperty("code", code.name()); // útil para clientes
        String traceId = org.slf4j.MDC.get("traceId");
        if (traceId != null) pd.setProperty("traceId", traceId);
    }

    /***
     * Handles the Product Not Found Exception and returns a standardized 404 error response.
     *
     * @param ex  the thrown ProductNotFoundException
     * @param req the current HTTP request
     * @return a ResponseEntity containing the ProblemDetail for NOT_FOUND
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ProductNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        enrichException(pd, req, ErrorCode.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    /***
     * Handles the Invalid Input Exception and returns a standardized 400 error response.
     *
     * @param ex  the thrown InvalidInputException
     * @param req the current HTTP request
     * @return a ResponseEntity containing the ProblemDetail for INVALID_INPUT
     */
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ProblemDetail> handleInvalidInput(InvalidInputException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        enrichException(pd, req, ErrorCode.INVALID_INPUT);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    /***
     * Handles bean validation failures triggered by @Valid on request body objects and returns a 400 error.
     *
     * @param ex  the thrown MethodArgumentNotValidException
     * @param req the current HTTP request
     * @return a ResponseEntity containing the ProblemDetail for VALIDATION_ERROR
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details.isEmpty() ? "Validation error" : details);
        enrichException(pd, req, ErrorCode.VALIDATION_ERROR);

        return ResponseEntity.badRequest().body(pd);
    }

    /***
     * Handles constraint violations triggered by @Validated on query or path parameters and returns a 400 error.
     *
     * @param ex  the thrown ConstraintViolationException
     * @param req the current HTTP request
     * @return a ResponseEntity containing the ProblemDetail for VALIDATION_ERROR
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, details.isEmpty() ? "Constraint violation" : details);
        enrichException(pd, req, ErrorCode.VALIDATION_ERROR);

        return ResponseEntity.badRequest().body(pd);
    }

    /***
     * Handles parameter type mismatches (e.g., non-numeric path variables when a number is expected) and returns a 400 error.
     *
     * @param ex  the thrown MethodArgumentTypeMismatchException
     * @param req the current HTTP request
     * @return a ResponseEntity containing the ProblemDetail for TYPE_MISMATCH
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Parameter '%s' value '%s' is not valid for type %s"
                .formatted(ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg);
        enrichException(pd, req, ErrorCode.TYPE_MISMATCH);

        return ResponseEntity.badRequest().body(pd);
    }

    /***
     * Handles failures caused by upstream services and returns a standardized 502 error response.
     *
     * @param ex  the thrown ExternalServiceException
     * @param req the current HTTP request
     * @return a ResponseEntity containing the ProblemDetail for UPSTREAM_FAILURE
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ProblemDetail> handleUpstream(ExternalServiceException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        enrichException(pd, req, ErrorCode.UPSTREAM_FAILURE);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(pd);
    }

    /***
     * Handles cases where no matching handler or resource is found and returns a standardized 404 error response.
     *
     * @param ex  the thrown NoHandlerFoundException or NoResourceFoundException
     * @param req the current HTTP request
     * @return a ResponseEntity containing the ProblemDetail for NOT_FOUND
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ProblemDetail> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        String msg = "No handler for %s %s".formatted(ex.getHttpMethod(), ex.getRequestURL());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, msg);
        enrichException(pd, req, ErrorCode.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    /***
     * Handles cases where the HTTP method used is not allowed for the requested resource and returns a standardized 405 error response.
     *
     * @param ex  the thrown HttpRequestMethodNotSupportedException
     * @param req the current HTTP request
     * @return a ResponseEntity containing the ProblemDetail for METHOD_NOT_ALLOWED
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
        enrichException(pd, req, ErrorCode.METHOD_NOT_ALLOWED);

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(pd);
    }

    /***
     * Handles unexpected unhandled exceptions and returns a standardized 500 error response.
     *
     * @param ex  the thrown generic Exception
     * @param req the current HTTP request
     * @return a ResponseEntity containing the ProblemDetail for UNEXPECTED_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        enrichException(pd, req, ErrorCode.UNEXPECTED_ERROR);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
}
