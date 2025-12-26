package com.sachet.parallel_asynchronous.model;

import com.sachet.parallel_asynchronous.configuration.repo.ProductsRepo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public class ProductRunnable implements Runnable{
    private Long productId;
    private final ProductsRepo productsRepo;
    private Product product;

    public ProductRunnable(ProductsRepo productsRepo, Long productId) {
        this.productsRepo = productsRepo;
        this.productId = productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    @Override
    public void run() {
        product = productsRepo.getReferenceById(productId);
    }
}
