package com.elifcelik.stockmanagement.productservice.repository.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Product getByProductIdAndDeletedFalse(Long productId);

    Optional<Product> findByProductId(Long productId);

    List<Product> getAllByDeletedFalse();

}
