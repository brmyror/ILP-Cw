package uk.ac.ed.acp.cw2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import uk.ac.ed.acp.cw2.controller.ServiceController;

@Getter
@Setter
public class IsInRegionRequest {
    @JsonProperty("position")
    private Position position;

    @JsonProperty("region")
    private Region region;

    public static Boolean errorHandler(IsInRegionRequest req) {
        Logger logger = ServiceController.getLogger();

        // Check if req is null
        if (req == null ) {
            if (ServiceController.VERBOSE) {
                logger.error("IsInRegionRequest itself null");
            } return true;
        }

        // Check if position has errors
        else if (Position.errorHandler(req.getPosition())) {
            return true;
        }
        //return false;
        // Check if region has errors
        else return Region.errorHandler(req.getRegion());
    }
}
