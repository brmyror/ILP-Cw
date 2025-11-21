package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for PositionPair request containing two positions.
 */
@Getter
@Setter
@Builder
public class PositionPair {
    @JsonProperty("position1")
    private LngLat lngLat1;

    @JsonProperty("position2")
    private LngLat lngLat2;
}
