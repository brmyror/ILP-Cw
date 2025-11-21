package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for a longitude and latitude position.
 */
@Getter
@Setter
@Builder
public class LngLat {
    @JsonProperty("lng")
    private Double lng;

    @JsonProperty("lat")
    private Double lat;

}