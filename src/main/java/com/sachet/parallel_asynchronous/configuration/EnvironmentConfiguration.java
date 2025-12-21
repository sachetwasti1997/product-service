package com.sachet.parallel_asynchronous.configuration;

import com.sachet.parallel_asynchronous.configuration.model.DatabaseConfiguration;
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
    private String productCallCron;
}
