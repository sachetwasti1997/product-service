package com.sachet.parallel_asynchronous.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sachet.parallel_asynchronous.configuration.EnvironmentConfiguration;
import com.sachet.parallel_asynchronous.executor.ProductCompletableFutureImpl;
import com.sachet.parallel_asynchronous.executor.ProductExecutorImpl;
import com.sachet.parallel_asynchronous.model.Product;
import com.sachet.parallel_asynchronous.model.ProductDto;
import com.sachet.parallel_asynchronous.model.ServerResponse;
import com.sachet.parallel_asynchronous.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

@RestController
public class ProductsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductsController.class);

    private final ProductService productService;
    private final ProductExecutorImpl productExecutor;
    private final ProductCompletableFutureImpl productCompletableFuture;
    private final ObjectMapper objectMapper;

    public ProductsController(ProductService productService, ProductExecutorImpl productExecutor,
                              ProductCompletableFutureImpl productCompletableFuture) {
        this.productService = productService;
        this.productExecutor = productExecutor;
        this.productCompletableFuture = productCompletableFuture;
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @GetMapping("/get-product-server")
    public void getProduct() {
        productService.getAndSaveProduct();
    }

    @GetMapping("/get-products")
    public List<Product> getProducti(@RequestBody List<Long> productIds) {
        return productService.retrieveProductInfo(productIds);
    }

    @GetMapping("/get-products-async")
    public List<Product> getProductAsync(@RequestBody List<Long> productIds) throws InterruptedException, ExecutionException {
        return productCompletableFuture.getProductsById(productIds);
    }

    public List<Product> retrieveProductInfoAsync(List<Long> productIds) throws InterruptedException, ExecutionException {
        CompletableFuture<Product> productCompletableFuture1, productCompletableFuture2;
        List<Product> productLists = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i<productIds.size(); i+=2) {
            long firstI = productIds.get(i);
            productCompletableFuture1 = CompletableFuture.supplyAsync(() -> productService.findProductById(firstI));
            if (i+1 < productIds.size()) {
                long secondI = productIds.get(i+1);
                productCompletableFuture2 = CompletableFuture.supplyAsync(() -> productService.findProductById(secondI));
                CompletableFuture.allOf(productCompletableFuture1, productCompletableFuture2).join();
                productLists.add(productCompletableFuture1.get());
                productLists.add(productCompletableFuture2.get());
            }else {
                productLists.add(productCompletableFuture1.get());
            }
        }
        long endTime = System.currentTimeMillis();
        LOGGER.info("Fetched Products in {} milliseconds", (endTime-startTime));
        return productLists;
    }

    public List<Product> getProduct(List<Long> productIds) throws ExecutionException, InterruptedException {
        CompletableFuture<Product> productCompletableFuture1, productCompletableFuture2;
        List<Product> productLists = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i<productIds.size(); i+=2) {
            if (i+1 < productIds.size()) {
                int I = i;
                Thread t1 = new Thread(() -> productLists.add(productService.findProductById(productIds.get(I))));
                Thread t2 = new Thread(() -> productLists.add(productService.findProductById(productIds.get(I+1))));
                t1.start();
                t2.start();
                t1.join();
                t2.join();
            }else {
                int I = i;
                Thread t1 = new Thread(() -> productLists.add(productService.findProductById(productIds.get(I))));
                t1.start();
                t1.join();
            }
        }
        long endTime = System.currentTimeMillis();
        LOGGER.info("Fetched Products in {} milliseconds", (endTime-startTime));
        return productLists;
    }
}
