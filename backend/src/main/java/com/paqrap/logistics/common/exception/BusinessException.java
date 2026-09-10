package com.paqrap.logistics.common.exception;

import lombok.Getter;

/**
 * Base runtime exception for business rules violations.
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = null;
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
