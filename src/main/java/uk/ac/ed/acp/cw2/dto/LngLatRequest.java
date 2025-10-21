package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@Builder
// creates the equals and hashcode methods to then be used in isInRegion method
// https://www.baeldung.com/java-lombok-equalsandhashcode
@EqualsAndHashCode(of = {"lng", "lat"})
public class LngLatRequest {
    @JsonProperty("lng")
    private Double lng;

    @JsonProperty("lat")
    private Double lat;

}