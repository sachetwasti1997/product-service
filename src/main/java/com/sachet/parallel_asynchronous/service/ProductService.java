package com.sachet.parallel_asynchronous.service;

import com.sachet.parallel_asynchronous.configuration.EnvironmentConfiguration;
import com.sachet.parallel_asynchronous.configuration.repo.ProductsRepo;
import com.sachet.parallel_asynchronous.controller.ProductsController;
import com.sachet.parallel_asynchronous.model.CacheCount;
import com.sachet.parallel_asynchronous.model.Product;
import com.sachet.parallel_asynchronous.model.Review;
import com.sachet.parallel_asynchronous.model.ServerResponse;
import com.sachet.parallel_asynchronous.utils.ProductUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private final RestTemplate restTemplate;
    private final EnvironmentConfiguration environmentConfiguration;
    private final ProductsRepo productsRepo;

    public ProductService(RestTemplate restTemplate,
                          EnvironmentConfiguration environmentConfiguration,
                          ProductsRepo productsRepo) {
        this.restTemplate = restTemplate;
        this.environmentConfiguration = environmentConfiguration;
        this.productsRepo = productsRepo;
    }

    @Scheduled(cron = "${product.config.productCallCron}")
    public List<Product> getAndSaveProduct() {
        LOGGER.info("Started the process to fetch product from the api: {}", environmentConfiguration.getServerUrl());
        CacheCount cacheCount = ProductUtils.readCacheCount();
        if (cacheCount == null) {
            return List.of();
        }
        if (cacheCount.getCount() >= cacheCount.getTotal()){
            LOGGER.info("Nothing more to read from the Api");
            return List.of();
        }
        ResponseEntity<ServerResponse> products = callApi(cacheCount);
        LOGGER.info("The list of objects received {}", products.getBody());
        mapProductAndReviewsSaveAll(products.getBody().getProducts());
        incrementCacheAndSave(cacheCount);
        return products.getBody().getProducts();
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

    private void mapProductAndReviewsSaveAll(List<Product> products) {
        for (Product p: products) {
            for (Review r: p.getReviews()) {
                r.setProduct(p);
            }
        }
        productsRepo.saveAll(products);
    }



}
