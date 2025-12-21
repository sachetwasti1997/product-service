package com.sachet.parallel_asynchronous.configuration;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public abstract class BeanConfigurationBase {

    private final AutowireCapableBeanFactory beanFactory;

    public BeanConfigurationBase(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    Object create(Class<?> type) {
        return this.beanFactory.createBean(type);
    }
}
