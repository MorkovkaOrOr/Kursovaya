package com.hibernate;

public class ShelfNotFoundException extends Exception {
    public static final String MESSAGE = "Shelf not found.";
    
    public ShelfNotFoundException() {
        super(MESSAGE);
    }
}
