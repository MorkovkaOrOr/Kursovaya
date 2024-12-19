package com.hibernate;

public class InvalidQuantityFormatException extends Exception {
    public static final String MESSAGE = "Invalid quantity format.";
    
    public InvalidQuantityFormatException() {
        super(MESSAGE);
    }
}