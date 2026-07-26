package com.elifcelik.stockmanagement.productservice.service.impl;

import com.elifcelik.stockmanagement.productservice.enums.Language;
import com.elifcelik.stockmanagement.productservice.exception.enums.FriendlyMessageCodes;
import com.elifcelik.stockmanagement.productservice.exception.exceptions.ProductAlreadyDeletedException;
import com.elifcelik.stockmanagement.productservice.exception.exceptions.ProductNotCreateException;
import com.elifcelik.stockmanagement.productservice.exception.exceptions.ProductNotFoundException;
import com.elifcelik.stockmanagement.productservice.repository.entity.Product;
import com.elifcelik.stockmanagement.productservice.repository.entity.ProductRepository;
import com.elifcelik.stockmanagement.productservice.request.ProductCreateRequest;
import com.elifcelik.stockmanagement.productservice.request.ProductUpdateRequest;
import com.elifcelik.stockmanagement.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    @Override
    public Product createProduct(Language language, ProductCreateRequest productCreateRequest) {
        log.debug("[{}] [createProduct] -> request: {}", this.getClass().getSimpleName(), productCreateRequest);
        try {
            Product product = Product.builder()
                    .productName(productCreateRequest.getProductName())
                    .quantity(productCreateRequest.getQuantity())
                    .price(productCreateRequest.getPrice())
                    .deleted(false)
                    .build();
            Product productResponse = productRepository.save(product);
            log.debug("[{}] [createProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
            return productResponse;
        }catch (Exception exception){
            throw new ProductNotCreateException(language, FriendlyMessageCodes.PRODUCT_NOT_CREATED, "product request: " + productCreateRequest.toString());
        }
    }

    @Override
    public Product getProduct(Language language, Long productId) {
        log.debug("[{}] [getProduct] -> productId: {}", this.getClass().getSimpleName(), productId);
        Product product = productRepository.getByProductIdAndDeletedFalse(productId);
        if (Objects.isNull(product)){
            throw new ProductNotFoundException(language, FriendlyMessageCodes.PRODUCT_NOT_FOUND, "product id: " + productId);
        }
        log.debug("[{}] [getProduct] -> response: {}", this.getClass().getSimpleName(), product);
        return product;
    }

    @Override
    public List<Product> getProducts(Language language) {
        log.debug("[{}] [getProducts]", this.getClass().getSimpleName());
        List<Product> products = productRepository.getAllByDeletedFalse();
        if (products.isEmpty()){
            throw new ProductNotFoundException(language, FriendlyMessageCodes.PRODUCT_NOT_FOUND, "products not found");
        }
        log.debug("[{}] [getProducts] -> product count: {}", this.getClass().getSimpleName(), products.size());
        return products;
    }

    @Override
    public Product updateProduct(Language language, Long productId, ProductUpdateRequest productUpdateRequest) {
        log.debug("[{}] [updateProduct] -> productId: {}", this.getClass().getSimpleName(), productId);
        Product product = getProduct(language, productId);
        product.setProductName(productUpdateRequest.getProductName());
        product.setPrice(productUpdateRequest.getPrice());
        product.setQuantity(productUpdateRequest.getQuantity());
        Product productResponse = productRepository.save(product);
        log.debug("[{}] [updateProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
        return productResponse;
    }

    @Override
    public Product deleteProduct(Language language, Long productId) {
        log.debug("[{}] [deleteProduct] -> productId: {}", this.getClass().getSimpleName(), productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(language, FriendlyMessageCodes.PRODUCT_NOT_FOUND, "product not found productId: " + productId));
        if (product.isDeleted()) {
            throw new ProductAlreadyDeletedException(language, FriendlyMessageCodes.PRODUCT_ALREADY_DELETED, "product already deleted productId: " + productId);
        }
        product.setDeleted(true);
        Product productResponse = productRepository.save(product);
        log.debug("[{}] [deleteProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
        return productResponse;
    }
}

