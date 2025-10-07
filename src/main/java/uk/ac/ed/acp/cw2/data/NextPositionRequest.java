package uk.ac.ed.acp.cw2.data;

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

    public static Boolean errorHandler(NextPositionRequest req) {
        // Check if start or angle is null
        if (req.getStart() == null || req.getAngle() == null) {
            return true;
        }
        else if (req.getStart().getLng() == null || req.getStart().getLat() == null) {
            return true;
        }
        // Longitude must be between -180 and 180
        else if (req.getStart().getLng() > 0 || req.getStart().getLat() < 0) {
            return true;
        } else {
            return false;
        }
    }
}
