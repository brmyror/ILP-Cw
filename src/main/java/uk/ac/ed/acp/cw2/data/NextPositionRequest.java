package uk.ac.ed.acp.cw2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import uk.ac.ed.acp.cw2.controller.ServiceController;

@Getter
@Setter
public class NextPositionRequest {
    @JsonProperty("start")
    private Position start;

    @JsonProperty("angle")
    private Double angle;

    public static Boolean errorHandler(NextPositionRequest req) {
        Logger logger = ServiceController.getLogger();

        // Check if req is null
        if (req == null) {
            if (ServiceController.VERBOSE) {
                logger.error("NextPositionRequest itself null");
            } return true;
        }

        else if (Position.errorHandler(req.getStart())) {
            if (ServiceController.VERBOSE) {
                logger.error("Start position has error");
            } return true;
        }

        // Check if angle or start has an error
        else if (Angle.errorHandler(req.getAngle())) {
            if (ServiceController.VERBOSE) {
                logger.error("Angle has error");
            } return true;
        } return false;
    }
}
