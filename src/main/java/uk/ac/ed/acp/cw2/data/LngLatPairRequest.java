package uk.ac.ed.acp.cw2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LngLatPairRequest {
    @JsonProperty("position1")
    private Position position1;

    @JsonProperty("position2")
    private Position position2;
}
