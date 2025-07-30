package com.jjas.similar_products.domain.port.output;

import com.jjas.similar_products.domain.model.Product;

import java.util.List;

public interface ExternalProductService {
    
    List<String> fetchSimilarProductIds(String productId);

    Product fetchProductDetail(String productId);

}
