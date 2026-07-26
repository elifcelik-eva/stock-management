package com.elifcelik.stockmanagement.productcacheservice.mapper;

import com.elifcelik.stockmanagement.productcacheservice.repository.entity.Product;
import com.elifcelik.stockmanagement.productcacheservice.response.ProductResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductResponseMapper {

    public static ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .quantity(product.getQuantity())
                .price(product.getPrice())
                .build();
    }
}