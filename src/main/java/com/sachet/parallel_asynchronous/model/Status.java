package com.sachet.parallel_asynchronous.model;

public enum Status {
    ORDER_CREATED("ORDER_CREATED"),
    ORDER_CANCELLED("ORDER_CANCELLED"),
    PAYMENT_COMPLETED("PAYMENT_COMPLETED");

    Status(String status) {
    }
}
