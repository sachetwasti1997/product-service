package com.sachet.parallel_asynchronous.exception;

public class JwtValidationFailedException extends RuntimeException{

    public JwtValidationFailedException(String message) {
        super(message);
    }
}
