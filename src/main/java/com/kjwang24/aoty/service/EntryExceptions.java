package com.kjwang24.aoty.service;

public class EntryExceptions {

    public static class DuplicateEntryException extends RuntimeException {
        public DuplicateEntryException(String message) {
            super(message);
        }
    }

    public static class ForbiddenUpdateException extends RuntimeException {
        public ForbiddenUpdateException(String message) {
            super(message);
        }
    }

}
