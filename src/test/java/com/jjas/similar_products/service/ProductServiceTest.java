package com.jjas.similar_products.service;

import com.jjas.similar_products.domain.model.Product;
import com.jjas.similar_products.domain.port.output.ExternalProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ExternalProductService externalProductService;

    @InjectMocks
    ProductService productService;

    @Test
    void shouldReturnSimilarProductsWhenAllProductsExist() {
        // GIVEN
        String id = "1";
        List<String> similarIds = List.of("2", "3");

        Product product2 = new Product("2", "Shirt", new BigDecimal("19.99"), true);
        Product product3 = new Product("3", "Pants", new BigDecimal("29.99"), true);

        when(externalProductService.fetchSimilarProductIds(id)).thenReturn(similarIds);
        when(externalProductService.fetchProductDetail("2")).thenReturn(product2);
        when(externalProductService.fetchProductDetail("3")).thenReturn(product3);

        // WHEN
        List<Product> result = productService.getSimilarProducts(id);

        // THEN
        assertThat(result).containsExactly(product2, product3);
    }

    @Test
    void shouldSkipNullProductsInSimilarList() {
        // GIVEN
        String id = "1";
        List<String> similarIds = List.of("2", "3");

        when(externalProductService.fetchSimilarProductIds(id)).thenReturn(similarIds);
        when(externalProductService.fetchProductDetail("2")).thenReturn(null);
        when(externalProductService.fetchProductDetail("3")).thenReturn(
                new Product("3", "Pants", new BigDecimal("29.99"), true));

        // WHEN
        List<Product> result = productService.getSimilarProducts(id);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("3");
    }

    @Test
    void shouldSkipProductsThatThrowException() {
        // GIVEN
        String id = "1";
        when(externalProductService.fetchSimilarProductIds(id)).thenReturn(List.of("2", "3"));

        when(externalProductService.fetchProductDetail("2"))
                .thenThrow(new RuntimeException("Timeout"));
        when(externalProductService.fetchProductDetail("3"))
                .thenReturn(new Product("3", "Pants", new BigDecimal("29.99"), true));

        // WHEN
        List<Product> result = productService.getSimilarProducts(id);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("3");
    }

    @Test
    void shouldReturnEmptyListWhenSimilarIdsIsEmpty() {
        // GIVEN
        String id = "1";
        when(externalProductService.fetchSimilarProductIds(id)).thenReturn(List.of());

        // WHEN
        List<Product> result = productService.getSimilarProducts(id);

        // THEN
        assertThat(result).isEmpty();
    }
}
