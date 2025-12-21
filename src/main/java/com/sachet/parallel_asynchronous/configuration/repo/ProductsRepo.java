package com.sachet.parallel_asynchronous.configuration.repo;

import com.sachet.parallel_asynchronous.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepo extends JpaRepository<Product, Long> {
}
