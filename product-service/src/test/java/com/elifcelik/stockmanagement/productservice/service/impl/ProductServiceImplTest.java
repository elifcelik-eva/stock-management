package com.elifcelik.stockmanagement.productservice.service.impl;

import com.elifcelik.stockmanagement.productservice.enums.Language;
import com.elifcelik.stockmanagement.productservice.exception.exceptions.ProductNotFoundException;
import com.elifcelik.stockmanagement.productservice.repository.entity.Product;
import com.elifcelik.stockmanagement.productservice.repository.entity.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductServiceImpl productService;
    private final Language language = Language.EN;
    private Product product;

    @BeforeEach
    void setup(){
        product = Product.builder()
                .productId(1L)
                .productName("Laptop")
                .price(50000.0)
                .quantity(5)
                .deleted(false)
                .build();
    }

    @Test
    void shouldReturnProduct_whenProductExists(){
        when(productRepository.getByProductIdAndDeletedFalse(1L))
                .thenReturn(product);
        Product result = productService.getProduct(language,1L);
        assertNotNull(result);
        assertEquals("Laptop", result.getProductName());
    }

    @Test
    void shouldThrowException_whenProductNotFound(){
        when(productRepository.getByProductIdAndDeletedFalse(1L))
                .thenReturn(null);
        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(language, 1L));
    }

    @Test
    void shouldCallRepositoryOnce_whenGetProductCalled(){
        when(productRepository.getByProductIdAndDeletedFalse(1L))
                .thenReturn(product);
        productService.getProduct(language, 1L);
        verify(productRepository, times(1))
                .getByProductIdAndDeletedFalse(1L);
    }
}
