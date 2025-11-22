package uk.ac.ed.acp.cw2.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.util.UUID;

@Getter
@Setter
public class DroneServicePointAvailability {
    @NotNull
    private UUID id;

    @NotNull
    private String droneId;

    @NotNull
    private Integer servicePointId;

    @NotNull
    private Integer dayOfWeek;

    @NotNull
    private Time startTime;

    @NotNull
    private Time endTime;
}
