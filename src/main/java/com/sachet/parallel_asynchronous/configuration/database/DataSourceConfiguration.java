package com.sachet.parallel_asynchronous.configuration.database;

import com.sachet.parallel_asynchronous.configuration.BeanConfigurationBase;
import com.sachet.parallel_asynchronous.configuration.EnvironmentConfiguration;
import com.sachet.parallel_asynchronous.configuration.model.DatabaseConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration extends BeanConfigurationBase {

    public DataSourceConfiguration(AutowireCapableBeanFactory beanFactory) {
        super(beanFactory);
    }
//
    @Bean(name = "productDS")
    public DataSource getProductDataSource(EnvironmentConfiguration environmentConfiguration) {
        DatabaseConfiguration databaseConfiguration = environmentConfiguration.getDatabaseConfiguration();
        return setUpDataBaseConnection(environmentConfiguration, databaseConfiguration);
    }
//
    private DataSource setUpDataBaseConnection(EnvironmentConfiguration environmentConfiguration,
                                               DatabaseConfiguration databaseConfiguration) {
//        try(HikariDataSource dataSource = new HikariDataSource()) {
//            dataSource.setJdbcUrl(databaseConfiguration.getUrl());
//            dataSource.setUsername(databaseConfiguration.getUserName());
//            dataSource.setPassword(databaseConfiguration.getPassword());
//            dataSource.setDriverClassName(databaseConfiguration.getDriverClassName());
//            dataSource.setMaximumPoolSize(databaseConfiguration.getMaxPoolSize());
////            dataSource.setPoolName(databaseConfiguration.getPoolName());
//            dataSource.setConnectionTimeout(databaseConfiguration.getConnectionTimeOut());
//            return dataSource;
//        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(databaseConfiguration.getUrl());
        hikariConfig.setUsername(databaseConfiguration.getUserName());
        hikariConfig.setPassword(databaseConfiguration.getPassword());
        hikariConfig.setMaximumPoolSize(databaseConfiguration.getMaxPoolSize());
        hikariConfig.setDriverClassName(databaseConfiguration.getDriverClassName());
        hikariConfig.setConnectionTimeout(databaseConfiguration.getConnectionTimeOut());
        return new HikariDataSource(hikariConfig);

//        return DataSourceBuilder
//                .create()
//                .url(databaseConfiguration.getUrl())
//                .driverClassName(databaseConfiguration.getDriverClassName())
//                .password(databaseConfiguration.getPassword())
//                .username(databaseConfiguration.getUserName())
//                .build();

    }
//
    @Bean(name = "productNameParameterJdbcTemplate")
    public JdbcTemplate getNamedParameterJdbcTemplate(EnvironmentConfiguration environmentConfiguration) {
        return new JdbcTemplate(getProductDataSource(environmentConfiguration));
    }

}
