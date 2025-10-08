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
        // Check if req is null
        if (req == null) {
            return true;
        }

        // Check if angle or start has an error
        else if (Angle.errorHandler(req.getAngle())) {
            return true;
        } else return Position.errorHandler(req.getStart());
    }
}
