package com.hibernate;

public class QuantityEmptyException extends Exception {
    public static final String MESSAGE = "Quantity cannot be empty.";
    
    public QuantityEmptyException() {
        super(MESSAGE);
    }
}
