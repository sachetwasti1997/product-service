package com.sachet.parallel_asynchronous.executor;

import com.sachet.parallel_asynchronous.configuration.repo.ProductsRepo;
import com.sachet.parallel_asynchronous.model.Product;
import com.sachet.parallel_asynchronous.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ProductCompletableFutureImpl {
    private final ProductsRepo productsRepo;
    private static final int BATCH_SIZE = Runtime.getRuntime().availableProcessors();
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductExecutorImpl.class);

    public ProductCompletableFutureImpl(ProductsRepo productsRepo) {
        this.productsRepo = productsRepo;
    }

    public List<Product> getProductsById(List<Long> ids) throws InterruptedException, ExecutionException {
        int k = 1;
        long startTime = System.currentTimeMillis();
        List<Product> products = new ArrayList<>();
        List<CompletableFuture<Product>> tasks = new ArrayList<>();
        int batches = (ids.size()+BATCH_SIZE-1)/BATCH_SIZE;
        int currentBatchStart = 0;
        while (batches > 0) {
            int currentBatchEnd = Math.min(ids.size() - currentBatchStart, BATCH_SIZE);
            for (k = currentBatchStart; k < currentBatchStart+currentBatchEnd; k++) {
                int currentIndex = k;
                tasks.add(CompletableFuture.supplyAsync(() -> productsRepo.findById(ids.get(currentIndex)).orElse(new Product())));
            }
            currentBatchStart = k;
            batches--;
            products.addAll(CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
                    .thenApply(result -> tasks.stream()
                            .map(f -> f.getNow(new Product()))
                            .toList())
                    .join());
            tasks.clear();
        }
        LOGGER.info("The process completed in {}", (System.currentTimeMillis() - startTime));
        return products;
    }

    @Async
    public CompletableFuture<List<Product>> getProductsByIdAsync(List<Long> ids) throws InterruptedException, ExecutionException {
        List<CompletableFuture<Product>> tasks = new ArrayList<>();
        for (long i: ids) {
            tasks.add(CompletableFuture.supplyAsync(() -> productsRepo.findById(i).orElse(new Product())));
        }
        return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
                .thenApply(result -> tasks.stream()
                        .map(f -> f.getNow(new Product()))
                        .toList());
    }
}
