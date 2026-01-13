package com.sachet.parallel_asynchronous.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ProductDto {

    private String title;
    private String description;
    private String category;
    private double price;
    private double discountPercentage;
    private double rating;
    private int stock;
    private String email;
    private List<String> tags;
    private List<Review> reviews;
    private List<String> images;
    private String thumbnail;

}
