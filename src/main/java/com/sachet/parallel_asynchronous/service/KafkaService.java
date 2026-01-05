package com.sachet.parallel_asynchronous.service;

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

    public KafkaService(EnvironmentConfiguration environmentConfiguration) {
        this.environmentConfiguration = environmentConfiguration;
    }

    @KafkaListener(topics = "user-add-product", groupId = "${spring.kafka.consumer.group-id}")
    public void consumer(String data) {
        LOGGER.info("The message: {}", data);
    }

}
