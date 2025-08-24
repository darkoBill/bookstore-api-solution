package com.bookstore.exception;

public class InvalidSortParameterException extends RuntimeException {
    
    public InvalidSortParameterException(String message) {
        super(message);
    }
}