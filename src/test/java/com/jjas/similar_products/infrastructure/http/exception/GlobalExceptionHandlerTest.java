package com.jjas.similar_products.infrastructure.http.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return 404 ProblemDetail for ProductNotFoundException")
    void testNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("NOT_FOUND")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Product not found: 123")))
                .andExpect(jsonPath("$.path", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail for InvalidInputException")
    void testInvalidInputException() throws Exception {
        mockMvc.perform(get("/test/invalid-input"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("INVALID_INPUT")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid input provided")));
    }

    @Test
    @DisplayName("Should return 502 ProblemDetail for ExternalServiceException")
    void testExternalServiceException() throws Exception {
        mockMvc.perform(get("/test/upstream-failure"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title", is("UPSTREAM_FAILURE")))
                .andExpect(jsonPath("$.status", is(502)))
                .andExpect(jsonPath("$.detail", is("External service error")));
    }

    @Test
    @DisplayName("Should return 500 ProblemDetail for generic exception")
    void testGenericException() throws Exception {
        mockMvc.perform(get("/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title", is("UNEXPECTED_ERROR")))
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.detail", is("Unexpected error")));
    }
    
}
