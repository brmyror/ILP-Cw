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

    public static Boolean errorHandler(LngLatPairRequest req) {
        // Check if req is null
        if (req == null) {
            return true;
        }

        // Check if pos1 or pos2 has an error
        else if (Position.errorHandler(req.getPosition1())) {
            return true;
        } else return Position.errorHandler(req.getPosition2());
    }
}
