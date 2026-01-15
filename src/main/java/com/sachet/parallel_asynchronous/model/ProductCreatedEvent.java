package com.sachet.parallel_asynchronous.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCreatedEvent {

    private Long userId;
    private Long productId;
    private Double price;

}
