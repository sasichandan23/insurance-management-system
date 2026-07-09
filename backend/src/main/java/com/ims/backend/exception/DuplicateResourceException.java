package com.ims.backend.exception;

/** Thrown when a unique constraint would be violated (e.g. duplicate email). */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
