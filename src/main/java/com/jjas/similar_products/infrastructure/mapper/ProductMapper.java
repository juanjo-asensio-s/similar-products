package com.jjas.similar_products.infrastructure.mapper;

import com.jjas.similar_products.domain.model.Product;
import com.jjas.similar_products.generated.similar.model.ProductDetail;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "availability", source = "availability")
    ProductDetail toDto(Product p);


    @IterableMapping(elementTargetType = ProductDetail.class)
    Set<ProductDetail> toDtoSet(Set<Product> products);
}
