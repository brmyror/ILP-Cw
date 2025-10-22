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
public class PositionPairRequest {
    @JsonProperty("position1")
    private LngLatRequest lngLatRequest1;

    @JsonProperty("position2")
    private LngLatRequest lngLatRequest2;
}
