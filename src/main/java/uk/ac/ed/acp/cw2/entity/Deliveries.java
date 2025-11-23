package uk.ac.ed.acp.cw2.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.ac.ed.acp.cw2.dto.LngLat;

@Getter
@Setter
@Builder
public class Deliveries {
    @JsonProperty("deliveryId")
    private Integer deliveryId;

    @JsonProperty
    private LngLat[] flightPath;
}