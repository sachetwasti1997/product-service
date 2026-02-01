package com.sachet.parallel_asynchronous.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class OrderDto {
    private long id;
    private String userId;
    private Status status;
    private Date expiresAt;
    private Long productId;
    private double price;
}
