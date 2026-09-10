package com.paqrap.logistics.common.exception;

import java.util.Map;
import lombok.Getter;

/**
 * Exception thrown for validation errors.
 */
@Getter
public class ValidationException extends BusinessException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, "VALIDATION_ERROR");
        this.fieldErrors = fieldErrors;
    }
}
