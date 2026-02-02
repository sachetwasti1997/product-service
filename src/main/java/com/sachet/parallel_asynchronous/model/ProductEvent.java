package com.sachet.parallel_asynchronous.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ProductEvent implements Serializable {

    private Long id;
    private String title;
    private double price;
    private int version;
    private int count;
    private String email;
    private String imageUrl;

}
