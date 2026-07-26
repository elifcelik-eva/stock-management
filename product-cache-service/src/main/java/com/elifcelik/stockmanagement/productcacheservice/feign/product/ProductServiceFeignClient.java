package com.elifcelik.stockmanagement.productcacheservice.feign.product;

import com.elifcelik.stockmanagement.productcacheservice.enums.Language;
import com.elifcelik.stockmanagement.productcacheservice.response.InternalApiResponse;
import com.elifcelik.stockmanagement.productcacheservice.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductServiceFeignClient {
    @GetMapping("/api/1.0/product/{language}/products/{productId}")
    InternalApiResponse<ProductResponse> getProduct(@PathVariable("language") Language language,
                                                    @PathVariable("productId") Long productId);

}
