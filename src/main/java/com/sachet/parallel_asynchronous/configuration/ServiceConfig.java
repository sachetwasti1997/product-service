package com.sachet.parallel_asynchronous.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableAsync
@Configuration
public class ServiceConfig {

//    @Bean(destroyMethod = "shutdown")
//    public ExecutorService executorService() {
//        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//    }
//
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor threadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("exe-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean(name = "poolFactory")
    public ThreadPoolExecutorFactoryBean threadPoolExecutorFactoryBean() {
        ThreadPoolExecutorFactoryBean bean = new ThreadPoolExecutorFactoryBean();
        bean.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        bean.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
        bean.setQueueCapacity(Runtime.getRuntime().availableProcessors());
        bean.setThreadNamePrefix("bean-async-");
        bean.setWaitForTasksToCompleteOnShutdown(true);
        bean.initialize();
        return bean;
    }

}
