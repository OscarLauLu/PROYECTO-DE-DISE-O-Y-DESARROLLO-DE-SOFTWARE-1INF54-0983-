package com.paqrap.logistics.simulation.engine;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Manages the simulated time in the simulation engine.
 */
@Component
public class SimulationClock {
    private volatile LocalDateTime currentTime;
    private double speedFactor = 1.0;
    private boolean running = false;
    private long tickIntervalMs = 1000;
    private LocalDateTime startTime;

    public void initialize(LocalDateTime startTime, double speedFactor) {
        this.startTime = startTime;
        this.currentTime = startTime;
        this.speedFactor = speedFactor;
        this.running = false;
    }

    public void tick() {
        if (running) {
            long simulatedMillisToAdvance = (long) (tickIntervalMs * speedFactor);
            this.currentTime = this.currentTime.plus(simulatedMillisToAdvance, ChronoUnit.MILLIS);
        }
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }

    public void pause() {
        this.running = false;
    }

    public void resume() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    public String getFormattedTime() {
        if (currentTime == null || startTime == null) return "N/A";
        return String.format("Día %d, %02d:%02d:%02d", 
                getCurrentDay(), 
                currentTime.getHour(), 
                currentTime.getMinute(), 
                currentTime.getSecond());
    }

    public int getCurrentDay() {
        if (currentTime == null || startTime == null) return 1;
        long days = Duration.between(startTime.toLocalDate().atStartOfDay(), 
                                     currentTime.toLocalDate().atStartOfDay()).toDays();
        return (int) days + 1;
    }
    
    public void setTickIntervalMs(long tickIntervalMs) {
        this.tickIntervalMs = tickIntervalMs;
    }
    
    public boolean isRunning() {
        return running;
    }
}
