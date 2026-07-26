package com.elifcelik.stockmanagement.productcacheservice.controller;

import com.elifcelik.stockmanagement.productcacheservice.enums.Language;
import com.elifcelik.stockmanagement.productcacheservice.mapper.ProductResponseMapper;
import com.elifcelik.stockmanagement.productcacheservice.repository.entity.Product;
import com.elifcelik.stockmanagement.productcacheservice.response.InternalApiResponse;
import com.elifcelik.stockmanagement.productcacheservice.response.ProductResponse;
import com.elifcelik.stockmanagement.productcacheservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/1.0/product-cache")
@Slf4j
@RequiredArgsConstructor
public class ProductCacheController {
    private final ProductService productService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{language}/products/{productId}")
    public InternalApiResponse<ProductResponse> getProduct(@PathVariable("language") Language language,
                                                           @PathVariable("productId") Long productId) {
        log.debug("[{}] [getProduct] -> productId: {}", this.getClass().getSimpleName(), productId);
        Product product = productService.getProduct(language, productId);
        ProductResponse productResponse = ProductResponseMapper.toProductResponse(product);
        log.debug("[{}] [getProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
        return InternalApiResponse.<ProductResponse>builder()
                .httpStatus(HttpStatus.OK)
                .hasError(false)
                .payload(productResponse)
                .build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{language}/products")
    public void removeProductsFromCache(@PathVariable("language") Language language) {
        log.debug("[{}] [removeProductsFromCache] -> language: {}", this.getClass().getSimpleName(), language);
        productService.deleteProductsFromCache();
        log.debug("[{}] [removeProductsFromCache] -> products removed from cache", this.getClass().getSimpleName());
    }
}
