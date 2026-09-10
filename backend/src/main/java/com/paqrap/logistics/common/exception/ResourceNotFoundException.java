package com.paqrap.logistics.common.exception;

import lombok.Getter;

/**
 * Exception thrown when a resource is not found.
 */
@Getter
public class ResourceNotFoundException extends BusinessException {
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s no encontrado con %s: '%s'", resourceName, fieldName, fieldValue), "RESOURCE_NOT_FOUND");
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
