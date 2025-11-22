package uk.ac.ed.acp.cw2.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.ac.ed.acp.cw2.dto.LngLat;

@Getter
@Setter
@Builder
public class DroneServicePoint {
    @NotNull
    private Integer id;

    @NotNull
    private String name;

    @NotNull
    private LngLat location;
}
