package com.sachet.parallel_asynchronous.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ServerResponse {

    List<Product> products;
    private int total;
    private int skip;
    private int limit;

}
