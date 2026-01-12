package com.sachet.parallel_asynchronous.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sachet.parallel_asynchronous.configuration.EnvironmentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class KafkaService {

    Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    private final EnvironmentConfiguration environmentConfiguration;
    private final ProductService productService;

    public KafkaService(EnvironmentConfiguration environmentConfiguration, ProductService productService) {
        this.environmentConfiguration = environmentConfiguration;
        this.productService = productService;
    }

    @KafkaListener(topics = "user-add-product", groupId = "${spring.kafka.consumer.group-id}")
    public void consumer(String data) {
        LOGGER.info("The message: {}", data);
    }

    @KafkaListener(topics = "user-product-review", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeReview(String data) {
        try {
            LOGGER.info("The message received is: {}", data);
            productService.saveProductReview(data);
        }catch (JsonProcessingException ex) {
            LOGGER.error("Caught an exception while reading event: {}", ex.getMessage());
        }
    }

}
