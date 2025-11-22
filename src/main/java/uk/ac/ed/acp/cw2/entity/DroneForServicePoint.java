package uk.ac.ed.acp.cw2.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class DroneForServicePoint {
    @NotNull
    private Integer servicePointId;
    @NotNull
    private DroneAvailability[] drones;

    @Getter
    @Setter
    @Builder
    public static class DroneAvailability {
        @NotNull
        private String id;
        @NotNull
        private Availability[] availability;
    }

    @Getter
    @Setter
    @Builder
    public static class Availability {
        @NotNull
        private DayOfWeek dayOfWeek;
        @NotNull
        private LocalTime from;
        @NotNull
        private LocalTime until;
    }
}