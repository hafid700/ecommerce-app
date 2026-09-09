package com.example.ecommerceapp.exception;

public class StockInsuffisantException extends RuntimeException {
    public StockInsuffisantException(String message) {
        super(message);
    }
}
