package com.jjas.similar_products.domain.port.input;

import com.jjas.similar_products.domain.model.Product;

import java.util.Set;

public interface ProductUseCase {

    Set<Product> findSimilarProducts(String productId);

}
