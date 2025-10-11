package uk.ac.ed.acp.cw2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import uk.ac.ed.acp.cw2.controller.ServiceController;

@Getter
@Setter
public class LngLatPairRequest {
    @JsonProperty("position1")
    private Position position1;

    @JsonProperty("position2")
    private Position position2;

    public static Boolean errorHandler(LngLatPairRequest req) {
        Logger logger = ServiceController.getLogger();

        // Check if req is null
        if (req == null) {
            if (ServiceController.VERBOSE) {
                logger.error("LngLatPairRequest itself null");
            } return true;
        }

        // Check if pos1 or pos2 has an error
        else if (Position.errorHandler(req.getPosition1())) {
            return true;
        } else return Position.errorHandler(req.getPosition2());
    }
}
