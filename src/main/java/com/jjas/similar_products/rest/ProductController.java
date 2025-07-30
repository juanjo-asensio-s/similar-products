package com.jjas.similar_products.rest;

import com.jjas.similar_products.domain.model.Product;
import com.jjas.similar_products.domain.port.input.ProductUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductUseCase productUseCase;

    public ProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<Product>> getSimilar(@PathVariable String id) {
        return ResponseEntity.ok(productUseCase.getSimilarProducts(id));
    }
    
}
