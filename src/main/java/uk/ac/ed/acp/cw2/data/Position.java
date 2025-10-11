package uk.ac.ed.acp.cw2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import uk.ac.ed.acp.cw2.controller.ServiceController;
import org.slf4j.Logger;

@Getter
@Setter
@EqualsAndHashCode(of = {"lng", "lat"})
public class Position {
    @JsonProperty("lng")
    private Double lng;

    @JsonProperty("lat")
    private Double lat;

    public static Boolean errorHandler(Position pos) {
        Logger logger = ServiceController.getLogger();

        // Check if pos is null
        if (pos == null) {
            if (ServiceController.VERBOSE) {
                logger.error("Position itself null");
            } return true;
        }

        // Check if lng or lat is null
        else if (pos.getLng() == null || pos.getLat() == null) {
            if (ServiceController.VERBOSE) {
                logger.error("Longitude or Latitude null");
            } return true;
        }

        // Check if lng or lat is NaN
        else if (pos.getLng().isNaN() || pos.getLat().isNaN()) {
            if (ServiceController.VERBOSE) {
                logger.error("Longitude or Latitude is NaN");
            } return true;
        }

        // Longitude must be between -180 and 180,
        else if (pos.getLng() > 180 || pos.getLng() < -180) {
            if (ServiceController.VERBOSE) {
                logger.error("Longitude out of range");
            } return true;
        }

        // Latitude must be between -90 and 90
        else if (pos.getLat() > 90 || pos.getLat() < -90) {
            if (ServiceController.VERBOSE) {
                logger.error("Latitude out of range");
            } return true;
        } return false;
    }
}