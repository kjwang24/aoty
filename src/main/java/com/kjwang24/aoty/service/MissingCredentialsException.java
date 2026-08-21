package com.kjwang24.aoty.service;

public class MissingCredentialsException extends RuntimeException {
    
    public MissingCredentialsException(String message) {
        super(message);
    }

}
