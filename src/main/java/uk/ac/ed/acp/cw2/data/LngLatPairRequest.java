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
        // Check if pos1 or pos2 is null
        if (req.getPosition1() == null || req.getPosition2() == null) {
            return true;
        }

        // check if pos 1, lng or lat is null
        else if (req.getPosition1().getLng() == null || req.getPosition1().getLat() == null) {
            return true;
        }

        // check if pos 2, lng or lat is null
        else if (req.getPosition2().getLng() == null || req.getPosition2().getLat() == null) {
            return true;
        }

        // Longitude must be between -180 and 180
        else if (req.getPosition1().getLng() > 0 || req.getPosition2().getLng() > 0
        || req.getPosition1().getLat() < 0 || req.getPosition2().getLat() < 0) {
            return true;
        } else {
            return false;
        }
    }
}
