package com.elifcelik.stockmanagement.productservice.service.impl;

import com.elifcelik.stockmanagement.productservice.enums.Language;
import com.elifcelik.stockmanagement.productservice.exception.enums.FriendlyMessageCodes;
import com.elifcelik.stockmanagement.productservice.exception.exceptions.ProductAlreadyDeletedException;
import com.elifcelik.stockmanagement.productservice.exception.exceptions.ProductNotCreateException;
import com.elifcelik.stockmanagement.productservice.exception.exceptions.ProductNotFoundException;
import com.elifcelik.stockmanagement.productservice.entity.Product;
import com.elifcelik.stockmanagement.productservice.mapper.ProductMapper;
import com.elifcelik.stockmanagement.productservice.repository.ProductRepository;
import com.elifcelik.stockmanagement.productservice.request.ProductCreateRequest;
import com.elifcelik.stockmanagement.productservice.request.ProductUpdateRequest;
import com.elifcelik.stockmanagement.productservice.response.ProductResponse;
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
    private final ProductMapper productMapper;
    @Override
    public ProductResponse createProduct(Language language, ProductCreateRequest productCreateRequest) {
        log.debug("[{}] [createProduct] -> request: {}", this.getClass().getSimpleName(), productCreateRequest);
        try {
            Product product = productMapper.toEntity(productCreateRequest);
            Product productResponse = productRepository.save(product);
            log.debug("[{}] [createProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
            return productMapper.toDto(productResponse);
        }catch (Exception exception){
            throw new ProductNotCreateException(language, FriendlyMessageCodes.PRODUCT_NOT_CREATED, "product request: " + productCreateRequest.toString());
        }
    }

    @Override
    public ProductResponse getProduct(Language language, Long productId) {
        log.debug("[{}] [getProduct] -> productId: {}", this.getClass().getSimpleName(), productId);
        Product product = productRepository.getByProductIdAndDeletedFalse(productId);
        if (Objects.isNull(product)){
            throw new ProductNotFoundException(language, FriendlyMessageCodes.PRODUCT_NOT_FOUND, "product id: " + productId);
        }
        ProductResponse productResponse = productMapper.toDto(product);
        log.debug("[{}] [getProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
        return productResponse;
    }

    @Override
    public List<ProductResponse> getProducts(Language language) {
        log.debug("[{}] [getProducts]", this.getClass().getSimpleName());
        List<Product> products = productRepository.getAllByDeletedFalse();
        if (products.isEmpty()){
            throw new ProductNotFoundException(language, FriendlyMessageCodes.PRODUCT_NOT_FOUND, "products not found");
        }
        log.debug("[{}] [getProducts] -> product count: {}", this.getClass().getSimpleName(), products.size());
        return productMapper.toDtoList(products);
    }

    @Override
    public ProductResponse updateProduct(Language language, Long productId, ProductUpdateRequest productUpdateRequest) {
        log.debug("[{}] [updateProduct] -> productId: {}", this.getClass().getSimpleName(), productId);
        Product product = productRepository.getByProductIdAndDeletedFalse(productId);
        if (Objects.isNull(product)){
            throw new ProductNotFoundException(language, FriendlyMessageCodes.PRODUCT_NOT_FOUND, "product id: " + productId);
        }
        Product updatedProduct = productMapper.updateEntity(product, productUpdateRequest);
        Product productResponse = productRepository.save(updatedProduct);
        log.debug("[{}] [updateProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
        return productMapper.toDto(productResponse);
    }

    @Override
    public ProductResponse deleteProduct(Language language, Long productId) {
        log.debug("[{}] [deleteProduct] -> productId: {}", this.getClass().getSimpleName(), productId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(language, FriendlyMessageCodes.PRODUCT_NOT_FOUND, "product not found productId: " + productId));
        if (product.isDeleted()) {
            throw new ProductAlreadyDeletedException(language, FriendlyMessageCodes.PRODUCT_ALREADY_DELETED, "product already deleted productId: " + productId);
        }
        product.setDeleted(true);
        Product savedProduct = productRepository.save(product);
        log.debug("[{}] [deleteProduct] -> response: {}", this.getClass().getSimpleName(), savedProduct.getProductId());
        return productMapper.toDto(savedProduct);
    }
}

