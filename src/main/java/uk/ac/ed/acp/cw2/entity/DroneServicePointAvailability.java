package uk.ac.ed.acp.cw2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "drone_service_point_availability", schema = "ilp")
public class DroneServicePointAvailability {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "drone_id", nullable = false, length = 50)
    private String droneId;

    @Column(name = "service_point_id", nullable = false)
    private Integer servicePointId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private Time startTime;

    @Column(name = "end_time", nullable = false)
    private Time endTime;

}
