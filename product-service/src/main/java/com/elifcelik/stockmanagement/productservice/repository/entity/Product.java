package com.elifcelik.stockmanagement.productservice.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product", schema = "stock_management")
public class Product {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;

    @Column(name = "product_created_date", updatable = false)
    private LocalDateTime productCreatedDate;

    @Column(name = "product_updated_date")
    private LocalDateTime productUpdatedDate;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate(){
        this.productCreatedDate = LocalDateTime.now();
        this.productUpdatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        this.productUpdatedDate = LocalDateTime.now();
    }

}
