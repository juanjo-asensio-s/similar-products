package com.jjas.similar_products.domain.port.output;

import com.jjas.similar_products.domain.model.Product;

import java.util.Set;

public interface ExternalProductService {

    Set<String> fetchSimilarProductIds(String productId);

    Product fetchProductDetail(String productId);

}
