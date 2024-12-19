package com.hibernate;

public class InvalidQuantityException extends Exception {
    public static final String MESSAGE = "Quantity must be greater than zero.";
    
    public InvalidQuantityException() {
        super(MESSAGE);
    }
}