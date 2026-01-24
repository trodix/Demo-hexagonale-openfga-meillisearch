package com.trodix.demo.application.exceptions;

public class ProductException extends RuntimeException {

    public ProductException(String message, Throwable cause, Object... msgParams) {
        super(String.format(message, msgParams), cause);
    }

    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductException(String message) {
        super(message);
    }

}
