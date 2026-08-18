package com.kjwang24.aoty.service;

public class ForbiddenUpdateException extends RuntimeException {

    public ForbiddenUpdateException(String message) {
        super(message);
    }

}
