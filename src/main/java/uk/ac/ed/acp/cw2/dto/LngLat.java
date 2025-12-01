package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for a longitude and latitude position.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LngLat {
    @JsonProperty("lng")
    private Double lng;

    @JsonProperty("lat")
    private Double lat;

}