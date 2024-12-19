package com.hibernate;

public class CustomException extends Exception {
    public CustomException() {
        super("You didn't write anything");
    }
    
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}