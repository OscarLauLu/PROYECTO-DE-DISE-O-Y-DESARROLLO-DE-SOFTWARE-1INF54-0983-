package com.paqrap.logistics.simulation.engine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event in the simulation timeline.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationEvent implements Comparable<SimulationEvent> {
    private LocalDateTime scheduledTime;
    private SimulationEventType type;
    private Map<String, Object> data;

    @Override
    public int compareTo(SimulationEvent o) {
        return this.scheduledTime.compareTo(o.scheduledTime);
    }
}
