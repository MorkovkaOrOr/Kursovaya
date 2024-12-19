package com.hibernate;

public class GenericException extends Exception {
    public static final String MESSAGE = "An error occurred while processing your request.";
    
    public GenericException() {
        super(MESSAGE);
    }
}