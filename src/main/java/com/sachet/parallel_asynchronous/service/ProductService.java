package com.sachet.parallel_asynchronous.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sachet.OrderDto;
import com.sachet.parallel_asynchronous.configuration.EnvironmentConfiguration;
import com.sachet.parallel_asynchronous.configuration.repo.CacheRepo;
import com.sachet.parallel_asynchronous.configuration.repo.ProductsRepo;
import com.sachet.parallel_asynchronous.configuration.repo.ReviewRepo;
import com.sachet.parallel_asynchronous.exception.JwtValidationFailedException;
import com.sachet.parallel_asynchronous.model.*;
import com.sachet.parallel_asynchronous.utils.ProductUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;

import static com.sachet.parallel_asynchronous.utils.SQLConstants.randomEmailList;

@Service
public class ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    private final RestTemplate restTemplate;
    private final EnvironmentConfiguration environmentConfiguration;
    private final ProductsRepo productsRepo;
    private final CacheRepo cacheRepo;
    private final ThreadPoolTaskExecutor executor;
    private final ExecutorService executorService;
    private int lastSuccessfulPhoto;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final ReviewRepo reviewRepo;
    private final KafkaTemplate<String, com.sachet.ProductDto> kafkaTemplate;

    public ProductService(RestTemplate restTemplate,
                          EnvironmentConfiguration environmentConfiguration,
                          ProductsRepo productsRepo,
                          CacheRepo cacheRepo,
                          JwtService jwtService,
                          @Qualifier("taskExecutor") ThreadPoolTaskExecutor executor,
                          JwtService jwtService1, ReviewRepo reviewRepo,
                         KafkaTemplate<String, com.sachet.ProductDto> kafkaTemplate) {
        this.restTemplate = restTemplate;
        this.environmentConfiguration = environmentConfiguration;
        this.productsRepo = productsRepo;
        this.cacheRepo = cacheRepo;
        this.executor = executor;
        this.executorService = executor.getThreadPoolExecutor();
        this.jwtService = jwtService1;
        this.reviewRepo = reviewRepo;
        this.kafkaTemplate = kafkaTemplate;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

        @Scheduled(cron = "${product.config.productCallCron}")
//    @EventListener(ApplicationReadyEvent.class)
    public void getAndSaveProduct() {
        try {
            CacheCount cacheCount = cacheRepo.findAll().getFirst();
            if (cacheCount.getCount() >= cacheCount.getTotal()) {
                LOGGER.info("Nothing more to read from the Api");
                return;
            }
            startFetchProcess(cacheCount);
        } catch (Exception e) {
            LOGGER.error("Caught Exception while reading products {}", e.getMessage());
        }
    }

    private void startFetchProcess(CacheCount cacheCount) {
        LOGGER.info("Started the process to fetch product from the api: {}", environmentConfiguration.getServerUrl());

        int startCount = cacheCount.getCount();
        int totalCount = cacheCount.getTotal();
        while (startCount < totalCount) {
            int limit = Math.min(totalCount - startCount, cacheCount.getFetchLimit());
            executorService.submit(() -> callApi(cacheCount.getCount(), limit));
            LOGGER.info("Submitted the task to fetch {} records starting from {}", limit, startCount);
            startCount += limit;
        }

        incrementCacheAndSave(cacheCount, startCount);
    }

    private synchronized void mapProductAndReviewsSaveAll(List<ProductDto> products, int id, int count) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String photoInfoUrl = environmentConfiguration.getImageServerUrl();
        Random rand = new Random();
        try {
            for (ProductDto productDto : products) {
                String seller_email = randomEmailList.get(rand.nextInt(0, 18));
                List<Images> images = new ArrayList<>();
                Product product = objectMapper.convertValue(productDto, Product.class);
                product.setEmail(seller_email);
                productDto.getImages().stream().forEach(image -> {
                    Images images1 = new Images();
                    images1.setUrl(image);
                    images1.setIsDownloaded(0);
                    images1.setProduct(product);
                    images.add(images1);
                });
//            images.addAll(new ArrayList<>(getImageInfo(id, photoInfoUrl, product)));
                product.setImagesDto(images);
                productsRepo.save(product);
                com.sachet.ProductDto event = new com.sachet.ProductDto();
                event.setPrice(product.getPrice());
                event.setVersion(0);
                event.setTitle(product.getTitle());
                event.setId(product.getId());
                event.setCount(product.getStock());
                event.setEmail(product.getEmail());
                event.setImageUrl(product.getImagesDto().get(0).getUrl());
                kafkaTemplate.send("user-add-product", product.getId().toString(), event)
                        .thenAccept(result -> {
                            LOGGER.info("Successfully sent the event {}", result);
                        }).join();
                id++;
            }
        }catch (Exception e) {
            LOGGER.info("Caught an exception {}", e.getMessage());
        }
    }

    private List<Images> getImageInfo(int id, String url, Product product) {
        String[] urls = url.split("_");
        PhotoInfo photoInfo = getPhoto(urls, id);
        PhotoInfo photoInfo2 = getPhoto(urls, 2 * id);
        Images image1 = new Images();
        Images image2 = new Images();
        image1.setUrl(photoInfo.getDownload_url());
        image1.setIsDownloaded(0);
        image1.setProduct(product);
        image2.setUrl(photoInfo2.getDownload_url());
        image2.setIsDownloaded(0);
        image2.setProduct(product);
        return List.of(image1, image2);
    }

    private PhotoInfo getPhoto(String[] urls, int id) {
        String url = urls[0] + id + urls[1];
        try {
            LOGGER.info("Downloading photo for the product from url {}", url);
            ResponseEntity<PhotoInfo> photoInfo = restTemplate.exchange(url, HttpMethod.GET, null, PhotoInfo.class);
            lastSuccessfulPhoto = id;
            return photoInfo.getBody();
        } catch (Exception e) {
            LOGGER.error("No photo found in {}", url);
            url = urls[0] + lastSuccessfulPhoto + urls[1];
            if (lastSuccessfulPhoto != 0) {
                LOGGER.info("Donloading alternate photo ");
                ResponseEntity<PhotoInfo> photoInfo = restTemplate.exchange(url, HttpMethod.GET, null, PhotoInfo.class);
                return photoInfo.getBody();
            }
            LOGGER.error("No photo found in {}", url);
        }
        PhotoInfo photoInfo = new PhotoInfo();
        photoInfo.setDownload_url("No Url");
        return photoInfo;
    }

    private synchronized @NonNull ResponseEntity<ServerResponse> callApi(int currentStart, int limit) throws JsonProcessingException {
        LOGGER.info("Calling the API {} to fetch {} records", environmentConfiguration.getServerUrl(), limit);

        ResponseEntity<ServerResponse> response = restTemplate.exchange(environmentConfiguration.getServerUrl() + "?skip=" + currentStart + "&limit=" + limit,
                HttpMethod.GET, null, ServerResponse.class);
        LOGGER.info("The response received from the server is {}, with the data {}", response.getStatusCode(), response.getBody().getProducts().size());
        ServerResponse serverResponse = response.getBody();
        int count = 0;
        mapProductAndReviewsSaveAll(serverResponse.getProducts(), currentStart, count);
        return response;
    }

    private void incrementCacheAndSave(CacheCount cacheCount, int currentCount) {
//        cacheCount.setCount(cacheCount.getCount());
//        ProductUtils.writeCacheCount(cacheCount);
        cacheCount.setCount(currentCount);
        LOGGER.info("Saving cache {}", cacheCount);
        cacheRepo.save(cacheCount);
    }

    public List<Product> retrieveProductInfo(List<Long> productIds) {
        long startTime = System.currentTimeMillis();
        List<Product> products = new ArrayList<>();
        for (Long id : productIds) {
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

        for (int i = 0; i < productIds.size(); i += 2) {
            long firstI = productIds.get(i);
            productCompletableFuture1 = CompletableFuture.supplyAsync(() -> productsRepo.findById(firstI).orElse(new Product()));
            if (i + 1 < productIds.size()) {
                long secondI = productIds.get(i + 1);
                productCompletableFuture2 = CompletableFuture.supplyAsync(() -> productsRepo.findById(secondI).orElse(new Product()));
                CompletableFuture.allOf(productCompletableFuture1, productCompletableFuture2).join();
                productLists.add(productCompletableFuture1.get());
                productLists.add(productCompletableFuture2.get());
            } else {
                productLists.add(productCompletableFuture1.get());
            }
        }
        return productLists;
    }

    public Product doOperationOnObject() {
        return new Product();
    }

    @Cacheable(value = "products-list", key = "#page")
    public List<Product> findAll(int page, int size) {
        PaginationRequest request = new PaginationRequest(page, size, "id", Sort.Direction.ASC);
        final Pageable pageable = ProductUtils.getPageable(request);
        final Page<Product> entities = productsRepo.findAll(pageable);
        return entities.get().toList();
    }

    public void saveProductReview(ReviewDto reviewDto, String token) {
//        ReviewDto reviewDto = objectMapper.readValue(jsonReview, ReviewDto.class);
//        Optional<Product> product = productsRepo.findById(reviewDto.getProductId());
//        if (product.isEmpty()){
//            LOGGER.info("No product found for the id {}, send in the review event",reviewDto.getProductId());
//            return;
//        }
//        Review review = objectMapper.convertValue(reviewDto, Review.class);
//        LOGGER.info("The review constructed from the submitted review by user: {}", review.getReviewerEmail());
//        review.setProduct(product.get());
//        reviewRepo.save(review);
//        LOGGER.info("Successfully saved the review!");
        String email = reviewDto.getReviewerEmail();
        if (!jwtService.validateToken(email, token)) {
            throw new JwtValidationFailedException("Invalid Jwt");
        }
        Review review = objectMapper.convertValue(reviewDto, Review.class);
        reviewRepo.save(review);
    }

    public long saveProduct(ProductDto productDto, String token) throws JsonProcessingException {
        Product product = objectMapper.convertValue(productDto, Product.class);
        String email = productDto.getEmail();
        token = token.substring(7);
        if (!jwtService.validateToken(email, token)) {
            throw new JwtValidationFailedException("Invalid Jwt");
        }
        productsRepo.save(product);
        com.sachet.ProductDto event = new com.sachet.ProductDto();
        event.setPrice(product.getPrice());
        event.setVersion(0);
        event.setTitle(product.getTitle());
        event.setId(product.getId());
        event.setCount(product.getStock());
        event.setEmail(product.getEmail());
        if (productDto.getImages() != null && !productDto.getImages().isEmpty())
            event.setImageUrl(productDto.getImages().get(0));
        LOGGER.info("The Event Constructed is {}", event);
        kafkaTemplate.send("user-add-product", product.getId().toString(), event)
                .thenAccept(result -> {
                    LOGGER.info("Successfully sent the event {}", result);
                }).join();
        return product.getId();
    }

//    public long updateProduct(ProductDto productDto, String token) throws JsonProcessingException {
//        Product product = objectMapper.convertValue(productDto, Product.class);
//        String email = productDto.getEmail();
//        if (!jwtService.validateToken(email, token)) {
//            throw new JwtValidationFailedException("Invalid Jwt");
//        }
//        productsRepo.save(product);
//        LOGGER.info("Saved the product with Id: {}", product.getId());
//        kafkaTemplate.send("update-product", objectMapper.writeValueAsString(product))
//                .thenAccept(result -> {
//                    LOGGER.info("Successfully sent the event {}", result);
//                }).join();
//        return product.getId();
//    }
//
//    public long updateProductQuantity(ProductDto productDto, String token) throws JsonProcessingException {
//        Product product = objectMapper.convertValue(productDto, Product.class);
//        String email = productDto.getEmail();
//        if (!jwtService.validateToken(email, token)) {
//            throw new JwtValidationFailedException("Invalid Jwt");
//        }
//        productsRepo.save(product);
//        LOGGER.info("Saved the product with Id: {}", product.getId());
//        kafkaTemplate.send("update-product", objectMapper.writeValueAsString(product))
//                .thenAccept(result -> {
//                    LOGGER.info("Successfully sent the event {}", result);
//                }).join();
//        return product.getId();
//    }

    public void orderCreated(OrderDto orderDto) throws JsonProcessingException {
        long productId = orderDto.getProductId();
        Optional<Product> product = productsRepo.findById(productId);
        Product productItem = product.get();
        productItem.setStock(productItem.getStock() - orderDto.getCount());
        productsRepo.save(productItem);
        LOGGER.info("Successfully reserved the product: {}", productItem);
    }

    public void orderExpired(OrderDto orderDto) throws JsonProcessingException {
        long productId = orderDto.getProductId();
        Optional<Product> product = productsRepo.findById(productId);
        Product productItem = product.get();
        productItem.setStock(productItem.getStock() + orderDto.getCount());
        productsRepo.save(productItem);
        LOGGER.info("Successfully unreserved the product: {}", productItem);
    }

}
