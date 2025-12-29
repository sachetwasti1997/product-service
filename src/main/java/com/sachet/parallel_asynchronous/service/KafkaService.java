package com.sachet.parallel_asynchronous.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class KafkaService {

    Logger LOGGER = LoggerFactory.getLogger(KafkaService.class);

    @KafkaListener(topics = "user-add-product", groupId = "group-sachet")
    public void consumer(String data) {
        LOGGER.info("The message: {}", data);
    }

}
