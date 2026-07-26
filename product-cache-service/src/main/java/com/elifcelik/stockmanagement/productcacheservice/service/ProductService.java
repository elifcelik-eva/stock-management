package com.elifcelik.stockmanagement.productcacheservice.service;

import com.elifcelik.stockmanagement.productcacheservice.enums.Language;
import com.elifcelik.stockmanagement.productcacheservice.repository.entity.Product;

public interface ProductService {

    Product getProduct(Language language, Long productId);

    void deleteProductsFromCache();
}
