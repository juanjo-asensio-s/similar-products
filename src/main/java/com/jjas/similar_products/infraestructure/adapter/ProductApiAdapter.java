package com.jjas.similar_products.infraestructure.adapter;

import com.jjas.similar_products.domain.model.Product;
import com.jjas.similar_products.domain.port.output.ExternalProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ProductApiAdapter implements ExternalProductService {

    private static final Logger LOGGER = Logger.getLogger(ProductApiAdapter.class.getName());

    private static final String SIMILAR_PRODUCTS_CIRCUIT = "similarProductIds";
    private static final String PRODUCT_DETAILS_CIRCUIT = "productDetails";

    private final WebClient webClient;

    public ProductApiAdapter(@Value("${external.product-api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /***
     * Method that uses flux to retrieve the similar Ids with a product ID given
     * @param productId
     * @return a String array with the Product IDs list
     */
    @Override
    @CircuitBreaker(name = SIMILAR_PRODUCTS_CIRCUIT, fallbackMethod = "fallbackSimilarIds")
    @Retry(name = SIMILAR_PRODUCTS_CIRCUIT)
    @Cacheable(cacheNames = "similar-ids", key = "#productId")
    public List<String> fetchSimilarProductIds(String productId) {
        LOGGER.log(Level.INFO, "Retrieving similar ids for product: " + productId);
        return webClient.get()
                .uri("/product/{id}/similarids", productId)
                .retrieve()
                .bodyToFlux(Integer.class)
                .map(String::valueOf)
                .collectList()
                .block();
    }

    /***
     * Method that uses flux to retrieve the product detail of a product ID given
     * @param productId
     * @return a Product object
     */
    @Override
    @CircuitBreaker(name = PRODUCT_DETAILS_CIRCUIT, fallbackMethod = "fallbackProduct")
    @Retry(name = PRODUCT_DETAILS_CIRCUIT)
    @Cacheable(cacheNames = "product", key = "#productId")
    public Product fetchProductDetail(String productId) {
        LOGGER.log(Level.INFO, "Retrieving product " + productId);
        try {
            return webClient.get()
                    .uri("/product/{id}", productId)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            response -> response
                                    .bodyToMono(String.class)
                                    .defaultIfEmpty("Error")
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "HTTP Error fetching product id=" + productId + ": " + body)))
                    )
                    .bodyToMono(Product.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch product details for id=" + productId, e);
        }
    }

    /***
     * Fallback method for errors retrieving the similar products
     * @param productId
     * @param ex
     * @return an empty list
     */
    public List<String> fallbackSimilarIds(String productId, Throwable ex) {
        LOGGER.log(Level.WARNING, String.format("Fallback triggered for similar IDs of product %s: %s", productId, ex.getMessage()));

        return Collections.emptyList();
    }

    /***
     * Fallback method for errors retrieving the sprodut details
     * @param productId
     * @param ex
     * @return a null product
     */
    public Product fallbackProduct(String productId, Throwable ex) {
        LOGGER.log(Level.WARNING, String.format("Fallback triggered for product details of %s: %s", productId, ex.getMessage()));

        return null;
    }

}