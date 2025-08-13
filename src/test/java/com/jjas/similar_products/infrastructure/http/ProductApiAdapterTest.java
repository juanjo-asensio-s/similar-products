package com.jjas.similar_products.infrastructure.http;

import com.jjas.similar_products.domain.model.Product;
import com.jjas.similar_products.generated.external.api.DefaultApi;
import com.jjas.similar_products.generated.external.model.ProductDetail;
import com.jjas.similar_products.infrastructure.http.exception.ExternalServiceException;
import com.jjas.similar_products.infrastructure.http.exception.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductApiAdapterTest {

    @Mock
    DefaultApi defaultApi;

    @InjectMocks
    ProductApiAdapter adapter;

    @Test
    @DisplayName("fetchSimilarProductIds returns product ID set")
    void shouldGetSimilarIds() {
        //GIVEN
        when(defaultApi.getProductSimilarids(eq("1"))).thenReturn(Set.of("2", "3"));

        //WHEN
        Set<String> ids = adapter.fetchSimilarProductIds("1");

        //THEN
        assertThat(ids).containsExactlyInAnyOrder("2", "3");
    }

    @Test
    @DisplayName("fetchProductDetail maps ProductDetail to domain")
    void shouldGetProductDetailOk() {
        //GIVEN
        ProductDetail ext = new ProductDetail()
                .id("2")
                .name("Prod 2")
                .price(new BigDecimal("12.34"))
                .availability(true);
        when(defaultApi.getProductProductId(eq("2"))).thenReturn(ext);

        //WHEN
        Product domain = adapter.fetchProductDetail("2");

        //THEN
        assertThat(domain.getId()).isEqualTo("2");
        assertThat(domain.getName()).isEqualTo("Prod 2");
        assertThat(domain.getPrice()).isEqualByComparingTo("12.34");
        assertThat(domain.getAvailability()).isTrue();
    }

    @Test
    @DisplayName("404 -> ProductNotFoundException")
    void shouldGetProductDetail404() {
        //GIVEN
        when(defaultApi.getProductProductId(eq("9")))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "not found", null, null, null));

        //THEN
        assertThatThrownBy(() -> adapter.fetchProductDetail("9"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("5xx -> ExternalServiceException")
    void shouldGetProductDetail5xx() {
        //GIVEN
        when(defaultApi.getProductProductId(eq("1")))
                .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY, "BG"));

        //THEN
        assertThatThrownBy(() -> adapter.fetchProductDetail("1"))
                .isInstanceOf(ExternalServiceException.class);
    }
    
}
