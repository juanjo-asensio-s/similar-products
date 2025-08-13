package com.jjas.similar_products.infrastructure.rest;

import com.jjas.similar_products.domain.model.Product;
import com.jjas.similar_products.domain.port.input.ProductUseCase;
import com.jjas.similar_products.generated.similar.api.ProductApi;
import com.jjas.similar_products.generated.similar.model.ProductDetail;
import com.jjas.similar_products.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController implements ProductApi {

    private final ProductUseCase productUseCase;

    private final ProductMapper productMapper;

    @GetMapping("/{productId}/similar")
    public ResponseEntity<Set<ProductDetail>> getProductSimilar(@PathVariable String productId) {
        Set<Product> productList = productUseCase.findSimilarProducts(productId);
        return ResponseEntity.ok(productMapper.toDtoSet(productList));
    }

}
