package com.mordiniaa.backend.exceptions;

public class TaskAlreadyUpdatedException extends RuntimeException {

    public TaskAlreadyUpdatedException() {
        super("Task Already Updated");
    }

    public TaskAlreadyUpdatedException(String message) {
        super(message);
    }
}
