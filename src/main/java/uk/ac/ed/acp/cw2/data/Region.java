package uk.ac.ed.acp.cw2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import uk.ac.ed.acp.cw2.controller.ServiceController;

@Getter
@Setter
public class Region {
    @JsonProperty("name")
    private String name;

    @JsonProperty("vertices")
    private Position[] vertices;

    public static Boolean errorHandler(Region region) {
        Logger logger = ServiceController.getLogger();

        // Check if region is null
        if (region == null) {
            if (ServiceController.VERBOSE) {
                logger.error("Region itself null");
            } return true;
        }

        // Check if name is null or blank
        else if (region.getName() == null || region.getName().isEmpty()) {
            if (ServiceController.VERBOSE) {
                logger.error("Region name null or blank");
            } return true;
        }

        // Check if vertices is null
        else if (region.getVertices() == null) {
            if (ServiceController.VERBOSE) {
                logger.error("Region vertices null");
            } return true;
        }

        // Check if vertices has less than 4 positions as region must be a closed polygon (triangle and closing point)
        else if (region.getVertices().length < 4) {
            if (ServiceController.VERBOSE) {
                logger.error("Region vertices has less than 4 positions");
            } return true;
        }

        // Check if the first and last positions exist and are the same (closed polygon)
        Position first = region.getVertices()[0];
        Position last = region.getVertices()[region.getVertices().length - 1];
        if (first == null || last == null) {
            if (ServiceController.VERBOSE) {
                logger.error("First or last position in vertices is null");
            } return true;
        }
        if (!first.equals(last)) {
            if (ServiceController.VERBOSE) {
                logger.error("First and last position in vertices are not the same");
            } return true;
        }

        // Check if any position in vertices has an error
        int index = 1;
        for (Position pos : region.getVertices()) {
            if (Position.errorHandler(pos)) {
                if (ServiceController.VERBOSE) {
                    logger.error("Position {} in vertices has an error", index);
                } return true;
            } index ++;
        }
        return false;
    }
}
