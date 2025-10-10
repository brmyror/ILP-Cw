package uk.ac.ed.acp.cw2.data;

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

    public static Boolean errorHandler(IsInRegionRequest req) {
        // Check if req is null
        if (req == null ) {
            return true;
        }

        // Check if position has errors
        else if (Position.errorHandler(req.getPosition())) {
            return true;
        }

        // Check if region has errors
        else return Region.errorHandler(req.getRegion());
    }
}
