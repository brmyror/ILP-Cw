package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NextPositionRequest {
    @JsonProperty("start")
    private Position start;

    @JsonProperty("angle")
    private Double angle;
}
