package com.sachet.parallel_asynchronous.configuration.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseConfiguration {
    private String url;
    private String readUrl;
    private String driverClassName;
    private String userName;
    private String password;
    private long connectionTimeOut;
    private long maxLifeTime;
    private int maxPoolSize;
    private String poolName;
}
