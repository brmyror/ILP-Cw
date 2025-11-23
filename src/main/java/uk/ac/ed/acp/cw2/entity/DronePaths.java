package uk.ac.ed.acp.cw2.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DronePaths {
    @JsonProperty("droneId")
    private Integer droneId;

    @JsonProperty("deliveries")
    private Deliveries[] deliveries;
}