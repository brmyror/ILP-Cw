package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for NextPosition request containing a start position and an angle.
 */
@Getter
@Setter
@Builder
public class NextPosition {
    @JsonProperty("start")
    private LngLat start;

    @JsonProperty("angle")
    private Double angle;
}
