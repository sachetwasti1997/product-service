package com.sachet.parallel_asynchronous.configuration;

import com.sachet.parallel_asynchronous.configuration.model.DatabaseConfiguration;
import com.sachet.parallel_asynchronous.configuration.model.KafkaConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "product.config")
public class EnvironmentConfiguration {
    private String profile;
    private String serverUrl;
    private DatabaseConfiguration databaseConfiguration;
    private KafkaConfiguration kafkaConfiguration;
    private String productCallCron;
    private String imageServerUrl;
}
