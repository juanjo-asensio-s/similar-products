package com.jjas.similar_products.infrastructure.rest;

import com.jjas.similar_products.domain.model.Product;
import com.jjas.similar_products.domain.port.input.ProductUseCase;
import com.jjas.similar_products.generated.similar.model.ProductDetail;
import com.jjas.similar_products.infrastructure.http.exception.ExternalServiceException;
import com.jjas.similar_products.infrastructure.http.exception.InvalidInputException;
import com.jjas.similar_products.infrastructure.http.exception.ProductNotFoundException;
import com.jjas.similar_products.infrastructure.mapper.ProductMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
class ProductControllerTest {

    @Resource
    private MockMvc mockMvc;

    @MockBean
    private ProductUseCase productUseCase;

    @MockBean
    private ProductMapper productMapper;

    @Test
    @DisplayName("GET /product/{id}/similar -> 200 with valid ProductDetail List")
    void shouldGetSimilarOk() throws Exception {
        String id = "1";
        Product domain = new Product("2", "Prod 2", new BigDecimal("12.34"), true);
        ProductDetail dto = new ProductDetail().id("2").name("Prod 2").price(new BigDecimal("12.34")).availability(true);

        Mockito.when(productUseCase.findSimilarProducts(eq(id))).thenReturn(Set.of(domain));
        Mockito.when(productMapper.toDtoSet(eq(Set.of(domain)))).thenReturn(Set.of(dto));

        mockMvc.perform(get("/product/{id}/similar", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("2")))
                .andExpect(jsonPath("$[0].name", is("Prod 2")))
                .andExpect(jsonPath("$[0].price", is(12.34)))
                .andExpect(jsonPath("$[0].availability", is(true)));
    }

    @Test
    @DisplayName("GET -> 404 when ProductNotFoundException")
    void shouldGetSimilarNotFound() throws Exception {
        Mockito.when(productUseCase.findSimilarProducts("999"))
                .thenThrow(new ProductNotFoundException("not found"));

        mockMvc.perform(get("/product/{id}/similar", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("GET -> 400 when no valid input")
    void shouldGetSimilarBadRequest() throws Exception {
        Mockito.when(productUseCase.findSimilarProducts("bad"))
                .thenThrow(new InvalidInputException("bad"));

        mockMvc.perform(get("/product/{id}/similar", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("INVALID_INPUT")));
    }

    @Test
    @DisplayName("GET -> 502 when external service fails")
    void shouldGetSimilarBadGateway() throws Exception {
        Mockito.when(productUseCase.findSimilarProducts("1"))
                .thenThrow(new ExternalServiceException("down", null));

        mockMvc.perform(get("/product/{id}/similar", "1"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title", is("UPSTREAM_FAILURE")));
    }

}
