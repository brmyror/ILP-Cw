package uk.ac.ed.acp.cw2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Position {
    @JsonProperty("lng")
    private Double lng;

    @JsonProperty("lat")
    private Double lat;
    }

