package com.paqrap.logistics.common.exception;

import java.time.LocalDateTime;
import lombok.Getter;

/**
 * Exception thrown when a delivery deadline is violated.
 */
@Getter
public class DeadlineViolationException extends BusinessException {
    private final Long orderId;
    private final LocalDateTime deadline;
    private final LocalDateTime estimatedArrival;

    public DeadlineViolationException(Long orderId, LocalDateTime deadline, LocalDateTime estimatedArrival) {
        super(String.format("Violación de plazo de entrega para pedido %d. Límite: %s, Llegada estimada: %s", orderId, deadline, estimatedArrival), "DEADLINE_VIOLATION");
        this.orderId = orderId;
        this.deadline = deadline;
        this.estimatedArrival = estimatedArrival;
    }
}
