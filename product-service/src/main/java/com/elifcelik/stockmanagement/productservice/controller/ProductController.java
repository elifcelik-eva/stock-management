package com.elifcelik.stockmanagement.productservice.controller;

import com.elifcelik.stockmanagement.productservice.enums.Language;
import com.elifcelik.stockmanagement.productservice.exception.enums.FriendlyMessageCodes;
import com.elifcelik.stockmanagement.productservice.exception.utils.FriendlyMessageUtils;
import com.elifcelik.stockmanagement.productservice.entity.Product;
import com.elifcelik.stockmanagement.productservice.request.ProductCreateRequest;
import com.elifcelik.stockmanagement.productservice.request.ProductUpdateRequest;
import com.elifcelik.stockmanagement.productservice.response.FriendlyMessage;
import com.elifcelik.stockmanagement.productservice.response.InternalApiResponse;
import com.elifcelik.stockmanagement.productservice.response.ProductResponse;
import com.elifcelik.stockmanagement.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/api/1.0/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{language}/products")
    public InternalApiResponse<ProductResponse> createProduct(@PathVariable("language")Language language,
                                                              @RequestBody ProductCreateRequest productCreateRequest){
        log.debug("[{}] [createProduct] -> request: {}", this.getClass().getSimpleName(), productCreateRequest);
        ProductResponse productResponse = productService.createProduct(language, productCreateRequest);
        log.debug("[{}] [createProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
        return InternalApiResponse.<ProductResponse>builder()
                .message(FriendlyMessage.builder()
                        .title(FriendlyMessageUtils.getFriendlyMessage(language, FriendlyMessageCodes.SUCCESS))
                        .description(FriendlyMessageUtils.getFriendlyMessage(language, FriendlyMessageCodes.PRODUCT_SUCCESSFULLY_CREATED))
                        .build())
                .httpStatus(HttpStatus.CREATED)
                .hasError(false)
                .payload(productResponse)
                .build();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{language}/products/{productId}")
    public InternalApiResponse<ProductResponse> getProduct(@PathVariable("language") Language language,
                                                           @PathVariable("productId") Long productId){
        log.debug("[{}] [getProduct] -> productId: {}", this.getClass().getSimpleName(), productId);
        ProductResponse productResponse = productService.getProduct(language,productId);
        log.debug("[{}] [getProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
        return InternalApiResponse.<ProductResponse>builder()
                .httpStatus(HttpStatus.OK)
                .hasError(false)
                .payload(productResponse)
                .build();
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{language}/products")
    public InternalApiResponse<List<ProductResponse>> getProducts(@PathVariable("language") Language language){
        log.debug("[{}] [getProducts]", this.getClass().getSimpleName());
        List<ProductResponse> productResponses = productService.getProducts(language);
        log.debug("[{}] [getProducts] -> product count: {}", this.getClass().getSimpleName(), productResponses.size());
        return InternalApiResponse.<List<ProductResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .hasError(false)
                .payload(productResponses)
                .build();
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{language}/products/{productId}")
    public InternalApiResponse<ProductResponse> updateProduct(@PathVariable("language") Language language,
                                                              @PathVariable("productId") Long productId,
                                                              @Valid @RequestBody ProductUpdateRequest productUpdateRequest){
        log.debug("[{}] [updateProduct] -> productId: {}, request: {}", this.getClass().getSimpleName(), productId, productUpdateRequest);
        ProductResponse productResponse = productService.updateProduct(language, productId, productUpdateRequest);
        log.debug("[{}] [updateProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
        return InternalApiResponse.<ProductResponse>builder()
                .message(FriendlyMessage.builder()
                        .title(FriendlyMessageUtils.getFriendlyMessage(language, FriendlyMessageCodes.SUCCESS))
                        .description(FriendlyMessageUtils.getFriendlyMessage(language, FriendlyMessageCodes.PRODUCT_SUCCESSFULLY_UPDATED))
                        .build())
                .httpStatus(HttpStatus.OK)
                .hasError(false)
                .payload(productResponse)
                .build();
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{language}/products/{productId}")
    public InternalApiResponse<ProductResponse> deleteProduct(@PathVariable("language") Language language,
                                                      @PathVariable("productId") Long productId){
        log.debug("[{}] [deleteProduct] -> productId: {}", this.getClass().getSimpleName(), productId);
        ProductResponse productResponse = productService.deleteProduct(language, productId);
        log.debug("[{}] [deleteProduct] -> response: {}", this.getClass().getSimpleName(), productResponse);
        return InternalApiResponse.<ProductResponse>builder()
                .message(FriendlyMessage.builder()
                        .title(FriendlyMessageUtils.getFriendlyMessage(language, FriendlyMessageCodes.SUCCESS))
                        .description(FriendlyMessageUtils.getFriendlyMessage(language, FriendlyMessageCodes.PRODUCT_SUCCESSFULLY_DELETED))
                        .build())
                .httpStatus(HttpStatus.OK)
                .hasError(false)
                .payload(productResponse)
                .build();
    }
}
