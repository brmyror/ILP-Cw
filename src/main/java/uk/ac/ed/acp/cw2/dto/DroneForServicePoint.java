package uk.ac.ed.acp.cw2.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DroneForServicePoint(Integer servicePointId, DroneAvailability[] drones) {
    public record DroneAvailability(String Id, Availability[] availability) {
    }

    public record Availability(DayOfWeek dayOfWeek, LocalTime from, LocalTime until) {}

}
