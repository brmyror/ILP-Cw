package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IsInRegionRequest {
    @JsonProperty("position")
    private Position position;

    @JsonProperty("region")
    private Region region;
}
