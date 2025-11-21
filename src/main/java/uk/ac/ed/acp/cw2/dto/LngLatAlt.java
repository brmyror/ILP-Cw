package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for a longitude, latitude, and altitude position.
 */
@Getter
@Setter
@Builder
public class LngLatAlt {
    @JsonProperty("lng")
    private Double lng;

    @JsonProperty("lat")
    private Double lat;

    @JsonProperty("alt")
    private Integer alt;

}