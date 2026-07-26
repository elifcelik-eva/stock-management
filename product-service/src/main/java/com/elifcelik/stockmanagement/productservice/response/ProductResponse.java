package com.elifcelik.stockmanagement.productservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ProductResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private LocalDateTime productCreatedDate;
    private LocalDateTime productUpdatedDate;
}
