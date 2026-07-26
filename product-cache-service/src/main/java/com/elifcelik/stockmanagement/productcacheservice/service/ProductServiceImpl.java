package com.elifcelik.stockmanagement.productcacheservice.service;

import com.elifcelik.stockmanagement.productcacheservice.enums.Language;
import com.elifcelik.stockmanagement.productcacheservice.exception.enums.FriendlyMessageCodes;
import com.elifcelik.stockmanagement.productcacheservice.exception.exceptions.ProductNotFoundException;
import com.elifcelik.stockmanagement.productcacheservice.exception.exceptions.ProductServiceUnavailableException;
import com.elifcelik.stockmanagement.productcacheservice.feign.product.ProductServiceFeignClient;
import com.elifcelik.stockmanagement.productcacheservice.repository.ProductRepository;
import com.elifcelik.stockmanagement.productcacheservice.repository.entity.Product;
import com.elifcelik.stockmanagement.productcacheservice.response.ProductResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductServiceFeignClient productServiceFeignClient;

    @Override
    public Product getProduct(Language language, Long productId) {
        Product product;
        try {
            Optional<Product> optionalProduct = productRepository.findById(productId);
            if (optionalProduct.isPresent()) {
                product = optionalProduct.get();
            } else {
                log.info("Product with id {} not found in cache, fetching from product service", productId);
                ProductResponse response = productServiceFeignClient.getProduct(language, productId).getPayload();
                product = Product.builder().productId(response.getProductId()).productName(response.getProductName()).quantity(response.getQuantity()).price(response.getPrice()).build();
                productRepository.save(product);
            }
        } catch (FeignException.FeignClientException.NotFound exception) {
            log.error("Product with id {} not found in product service", productId);
            throw new ProductNotFoundException(language, FriendlyMessageCodes.PRODUCT_NOT_FOUND, "Product not found: " + productId);
        } catch (FeignException e) {
            log.error("Feign error while fetching product {}: {}", productId, e.getMessage());
            throw new ProductServiceUnavailableException(language, FriendlyMessageCodes.PRODUCT_SERVICE_UNAVAILABLE,  "Feign error: " + e.getMessage());
        }
        return product;
    }

    @Override
    public void deleteProductsFromCache() {
        productRepository.deleteAll();
        log.info("Deleted all products from cache");
    }
}
