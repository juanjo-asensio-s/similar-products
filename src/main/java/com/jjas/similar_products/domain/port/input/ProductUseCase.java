package com.jjas.similar_products.domain.port.input;

import com.jjas.similar_products.domain.model.Product;

import java.util.List;

public interface ProductUseCase {

    List<Product> getSimilarProducts(String productId);

}
