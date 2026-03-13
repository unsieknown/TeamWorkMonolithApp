package com.mordiniaa.backend.exceptions;

public class TaskCommentNotFound extends RuntimeException {

    public TaskCommentNotFound() {
        super("Task Comment Not Found");
    }

    public TaskCommentNotFound(String message) {
        super(message);
    }
}
