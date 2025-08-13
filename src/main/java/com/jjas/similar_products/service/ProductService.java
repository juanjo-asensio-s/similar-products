package com.jjas.similar_products.service;

import com.jjas.similar_products.domain.model.Product;
import com.jjas.similar_products.domain.port.input.ProductUseCase;
import com.jjas.similar_products.domain.port.output.ExternalProductService;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ProductService implements ProductUseCase {

    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());

    private final ExternalProductService externalProductService;

    public ProductService(ExternalProductService externalProductService) {
        this.externalProductService = Objects.requireNonNull(externalProductService, "ExternalProductService cannot be null");
    }

    /***
     * Get a list of related products from a product ID
     * @param productId
     * @return the list of Products
     */
    @Override
    public Set<Product> findSimilarProducts(String productId) {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        LOGGER.log(Level.INFO, String.format("Finding similar products from: " + productId));

        Set<String> similarProductIds = externalProductService.fetchSimilarProductIds(productId);

        return similarProductIds.stream()
                .map(this::fetchProduct)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /***
     * Fetch a product by its ID
     * @param productId
     * @return a Product
     */
    private Product fetchProduct(String productId) {
        try {
            LOGGER.log(Level.INFO, String.format("Finding product detail of " + productId));

            return externalProductService.fetchProductDetail(productId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, String.format("Error retrieving product: %s -> %s", productId, ex.getMessage()));
            return null;
        }
    }

}