package com.hibernate;

public class RoomCapacityExceededException extends Exception {
    public static final String MESSAGE = "The selected room has reached its maximum shelf capacity.";
    
    public RoomCapacityExceededException() {
        super(MESSAGE);
    }
}