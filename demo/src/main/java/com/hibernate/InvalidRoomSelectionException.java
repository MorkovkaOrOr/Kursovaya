package com.hibernate;

public class InvalidRoomSelectionException extends Exception {
    public static final String MESSAGE = "Please select a valid Room.";
    
    public InvalidRoomSelectionException() {
        super(MESSAGE);
    }
}