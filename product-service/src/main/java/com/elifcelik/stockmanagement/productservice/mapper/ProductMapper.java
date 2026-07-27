package com.elifcelik.stockmanagement.productservice.mapper;

import com.elifcelik.stockmanagement.productservice.entity.Product;
import com.elifcelik.stockmanagement.productservice.request.ProductCreateRequest;
import com.elifcelik.stockmanagement.productservice.request.ProductUpdateRequest;
import com.elifcelik.stockmanagement.productservice.response.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    public Product toEntity(ProductCreateRequest createRequest) {
        if (createRequest == null) return null;
       return Product.builder()
                .productName(createRequest.getProductName())
                .quantity(createRequest.getQuantity())
                .price(createRequest.getPrice())
                .deleted(false)
                .build();
    }

    public Product updateEntity(Product product, ProductUpdateRequest updateRequest) {
        product.setProductName(updateRequest.getProductName());
        product.setQuantity(updateRequest.getQuantity());
        product.setPrice(updateRequest.getPrice());
        return product;
    }

    public ProductResponse toDto(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .quantity(product.getQuantity())
                .price(product.getPrice())
                .productCreatedDate(product.getProductCreatedDate())
                .productUpdatedDate(product.getProductUpdatedDate())
                .build();
    }

    public List<ProductResponse> toDtoList(List<Product> products) {
        return products.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
