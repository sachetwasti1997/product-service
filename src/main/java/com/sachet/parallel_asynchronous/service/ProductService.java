package com.sachet.parallel_asynchronous.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sachet.parallel_asynchronous.configuration.EnvironmentConfiguration;
import com.sachet.parallel_asynchronous.configuration.repo.ProductsRepo;
import com.sachet.parallel_asynchronous.configuration.repo.ReviewRepo;
import com.sachet.parallel_asynchronous.controller.ProductsController;
import com.sachet.parallel_asynchronous.model.*;
import com.sachet.parallel_asynchronous.utils.ProductUtils;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private final RestTemplate restTemplate;
    private final EnvironmentConfiguration environmentConfiguration;
    private final ProductsRepo productsRepo;
    private final ReviewRepo reviewRepo;

    public ProductService(RestTemplate restTemplate,
                          EnvironmentConfiguration environmentConfiguration,
                          ProductsRepo productsRepo,
                          ReviewRepo reviewRepo) {
        this.restTemplate = restTemplate;
        this.environmentConfiguration = environmentConfiguration;
        this.productsRepo = productsRepo;
        this.reviewRepo = reviewRepo;
    }

    @Scheduled(cron = "${product.config.productCallCron}")
    public void getAndSaveProduct() {
        LOGGER.info("Started the process to fetch product from the api: {}", environmentConfiguration.getServerUrl());
        CacheCount cacheCount = ProductUtils.readCacheCount();
        if (cacheCount == null) {
            return ;
        }
        if (cacheCount.getCount() >= cacheCount.getTotal()){
            LOGGER.info("Nothing more to read from the Api");
            return ;
        }
        ResponseEntity<ServerResponse> products = callApi(cacheCount);
        LOGGER.info("The list of objects received {}", products.getBody());

        mapProductAndReviewsSaveAll(products.getBody().getProducts());
        incrementCacheAndSave(cacheCount);
    }

    private void mapProductAndReviewsSaveAll(List<ProductDto> products) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String photoInfoUrl = environmentConfiguration.getImageServerUrl();
        int id = 0;
        for (ProductDto productDto: products) {
            Product product = objectMapper.convertValue(productDto, Product.class);
            List<Images> images = getImageInfo(id, photoInfoUrl, product);
            product.setImages(images);
            productsRepo.save(product);
        }
    }

    private List<Images> getImageInfo(int id, String url, Product product) {
        String[] urls = url.split("_");
        Images image1 = new Images();
        Images image2 = new Images();
        image1.setUrl(urls[0]+id+urls[1]);
        image1.setUrl(urls[0]+2*id+urls[1]);
        return List.of(image1, image2);
    }

    private @NonNull ResponseEntity<ServerResponse> callApi(CacheCount cacheCount) {
        int limit = Math.min(cacheCount.getTotal() - cacheCount.getCount(), cacheCount.getLimit());
        LOGGER.info("Calling the API {} to fetch {} records", environmentConfiguration.getServerUrl(), limit);

        ResponseEntity<ServerResponse> response = restTemplate.exchange(environmentConfiguration.getServerUrl() + "?skip=" + cacheCount.getCount() + "&limit=" + limit,
                HttpMethod.GET, null, ServerResponse.class);
        cacheCount.setCount(cacheCount.getCount() + limit);
        return response;
    }

    private void incrementCacheAndSave(CacheCount cacheCount) {
        cacheCount.setCount(cacheCount.getCount());
        ProductUtils.writeCacheCount(cacheCount);
    }

    public List<Product> retrieveProductInfo(List<Long> productIds) {
        long startTime = System.currentTimeMillis();
        List<Product> products = new ArrayList<>();
        for (Long id: productIds) {
            products.add(productsRepo.getReferenceById(id));
        }
        long endTime = System.currentTimeMillis();
        LOGGER.info("All the products fetched in {}", (endTime - startTime));
        return products;
    }

    public Product findProductById(Long id) {
        return productsRepo.findById(id).get();
    }

    public List<Product> findProductById(List<Long> productIds) throws InterruptedException, ExecutionException {
        CompletableFuture<Product> productCompletableFuture1, productCompletableFuture2;
        List<Product> productLists = new ArrayList<>();

        for (int i = 0; i<productIds.size(); i+=2) {
            long firstI = productIds.get(i);
            productCompletableFuture1 = CompletableFuture.supplyAsync(() -> productsRepo.findById(firstI).orElse(new Product()));
            if (i+1 < productIds.size()) {
                long secondI = productIds.get(i+1);
                productCompletableFuture2 = CompletableFuture.supplyAsync(() -> productsRepo.findById(secondI).orElse(new Product()));
                CompletableFuture.allOf(productCompletableFuture1, productCompletableFuture2).join();
                productLists.add(productCompletableFuture1.get());
                productLists.add(productCompletableFuture2.get());
            }else {
                productLists.add(productCompletableFuture1.get());
            }
        }
        return productLists;
    }

    public Product doOperationOnObject(){return new Product();}

}
