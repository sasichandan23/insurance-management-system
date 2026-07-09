package com.ims.backend.exception;

/** Thrown when a request violates a business rule
 *  (e.g. claim amount exceeds coverage, policy not active). */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
