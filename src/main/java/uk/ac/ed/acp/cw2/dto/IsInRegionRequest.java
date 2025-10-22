package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for IsInRegion request containing a position and a region.
 */
@Getter
@Setter
@Builder
public class IsInRegionRequest {
    @JsonProperty("position")
    private LngLatRequest lngLatRequest;

    @JsonProperty("region")
    private RegionRequest region;
}
