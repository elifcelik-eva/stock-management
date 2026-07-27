package com.elifcelik.stockmanagement.productservice.service;

import com.elifcelik.stockmanagement.productservice.enums.Language;
import com.elifcelik.stockmanagement.productservice.entity.Product;
import com.elifcelik.stockmanagement.productservice.request.ProductCreateRequest;
import com.elifcelik.stockmanagement.productservice.request.ProductUpdateRequest;
import com.elifcelik.stockmanagement.productservice.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(Language language, ProductCreateRequest productCreateRequest);

    ProductResponse getProduct(Language language, Long productId);

    List<ProductResponse> getProducts(Language language);

    ProductResponse updateProduct(Language language, Long productId, ProductUpdateRequest productUpdateRequest);

    ProductResponse deleteProduct(Language language, Long productId);

}
