package com.sachet.parallel_asynchronous.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CacheCount {
    private int count;
    private int limit;
    private int total;
    private int recordsFetched;
}
