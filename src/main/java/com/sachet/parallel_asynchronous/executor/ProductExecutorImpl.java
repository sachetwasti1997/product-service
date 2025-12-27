package com.sachet.parallel_asynchronous.executor;

import com.sachet.parallel_asynchronous.configuration.repo.ProductsRepo;
import com.sachet.parallel_asynchronous.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ProductExecutorImpl {

    private final ProductsRepo productsRepo;
    private static final int BATCH_SIZE = Runtime.getRuntime().availableProcessors();
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductExecutorImpl.class);

    public ProductExecutorImpl( ProductsRepo productsRepo) {
        this.productsRepo = productsRepo;
    }

    public List<Product> getProductsById(List<Long> ids) throws InterruptedException, ExecutionException {
//        int k = 1;
//        long startTime = System.currentTimeMillis();
//        List<Product> products = new ArrayList<>();
//        List<Callable<Product>> tasks = new ArrayList<>();
//        int batches = (ids.size() + BATCH_SIZE - 1)/BATCH_SIZE;
//        int currentBatchStart = 0;
//        while (batches > 0) {
//            int currentBatchEnd = Math.min(ids.size() - currentBatchStart, BATCH_SIZE);
//            for (k = currentBatchStart; k < currentBatchStart+currentBatchEnd; k++) {
//                int currentIndex = k;
//                tasks.add(() -> productsRepo.findById(ids.get(currentIndex)).orElse(new Product()));
//            }
////            LOGGER.info("Fetching current batch that starts from {}, with size {}", currentBatchStart, currentBatchEnd);
////            List<Future<Product>> productFutures = executor.invokeAll(tasks);
//            for (Future<Product> futures: productFutures) {
//                products.add(futures.get());
//            }
//            currentBatchStart = k;
//            batches--;
//        }
//        LOGGER.info("The process completed in {}", (System.currentTimeMillis() - startTime));
//        return products;
//    }
        return new ArrayList<>();
    }
}
