package uk.ac.ed.acp.cw2.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO for DroneServicePointAvailability.
 */
public record DroneServicePointAvailability(UUID id, String droneId, Integer servicePointId, DayOfWeek dayOfWeek, LocalTime from, LocalTime until) {
}
