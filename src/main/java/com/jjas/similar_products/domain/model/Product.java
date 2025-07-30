package com.jjas.similar_products.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private BigDecimal price;
    private Boolean availability;

}
