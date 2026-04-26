package com.sachet.parallel_asynchronous.configuration.kafka.producer;

import com.sachet.ProductDto;
import com.sachet.parallel_asynchronous.configuration.EnvironmentConfiguration;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private ProducerFactory<String, ProductDto> setUpAvroProducerFactory(EnvironmentConfiguration environmentConfiguration) {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, environmentConfiguration.getKafkaConfiguration().getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class,
                "schema.registry.url", environmentConfiguration.getKafkaConfiguration().getSchemaRegistryUrl()
        );
        return new DefaultKafkaProducerFactory<>(props);
    }

//    @Bean(name = "kafkaTemplate")
//    public KafkaTemplate<String, ProductDto> kafkaAvroTemplate(@Autowired EnvironmentConfiguration configuration) {
//        return new KafkaTemplate<>(setUpAvroProducerFactory(configuration));
//    }

}
