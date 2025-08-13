package com.jjas.similar_products.infrastructure.http;

import com.jjas.similar_products.domain.model.Product;
import com.jjas.similar_products.domain.port.output.ExternalProductService;
import com.jjas.similar_products.generated.external.api.DefaultApi;
import com.jjas.similar_products.generated.external.model.ProductDetail;
import com.jjas.similar_products.infrastructure.http.exception.ExternalServiceException;
import com.jjas.similar_products.infrastructure.http.exception.ProductNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class ProductApiAdapter implements ExternalProductService {
    private static final Logger LOGGER = Logger.getLogger(ProductApiAdapter.class.getName());

    private static final String CIRCUIT_BRAKER_NAME = "externalService";

    private final DefaultApi productsApi;

    @Override
    @Cacheable(value = "similar-ids", key = "#productId")
    @CircuitBreaker(name = CIRCUIT_BRAKER_NAME, fallbackMethod = "fallbackSimilarIds")
    @Retry(name = CIRCUIT_BRAKER_NAME)
    public Set<String> fetchSimilarProductIds(String productId) {
        try {
            Set<String> productSimilarids = productsApi.getProductSimilarids(productId);

            return productSimilarids;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(productId);
        } catch (HttpClientErrorException e) {
            throw new ExternalServiceException("Client error fetching similar IDs", e);
        } catch (HttpServerErrorException e) {
            throw new ExternalServiceException("Server error fetching similar IDs", e);
        } catch (Exception e) {
            throw new ExternalServiceException("Unexpected error fetching similar IDs", e);
        }
    }


    @Override
    @Cacheable(value = "product-details", key = "#productId")
    @CircuitBreaker(name = CIRCUIT_BRAKER_NAME, fallbackMethod = "fallbackProductDetail")
    @Retry(name = CIRCUIT_BRAKER_NAME)
    public Product fetchProductDetail(String productId) {
        try {
            ProductDetail productDetail = productsApi.getProductProductId(productId);

            return new Product(productDetail.getId(), productDetail.getName(), productDetail.getPrice(), productDetail.getAvailability());
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(productId);
        } catch (HttpClientErrorException e) {
            throw new ExternalServiceException("Client error fetching product", e);
        } catch (HttpServerErrorException e) {
            throw new ExternalServiceException("Server error fetching product", e);
        } catch (Exception e) {
            throw new ExternalServiceException("Unexpected error fetching product", e);
        }
    }

    // FALLBACKS CIRCUIT BRAKER

    /***
     * Fallback method for errors retrieving the similar products
     * @param productId
     * @param ex
     * @return an empty set
     */
    public Set<String> fallbackSimilarIds(String productId, Throwable ex) {
        LOGGER.log(Level.WARNING, String.format("Fallback triggered for similar IDs of product %s: %s", productId, ex.getMessage()));

        return Collections.emptySet();
    }

    /***
     * Fallback method for errors retrieving the product details
     * @param productId
     * @param ex
     * @return a null product
     */
    public Product fallbackProductDetail(String productId, Throwable ex) {
        LOGGER.log(Level.WARNING, String.format("Fallback triggered for product details of %s: %s", productId, ex.getMessage()));

        return null;
    }

}
