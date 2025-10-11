package uk.ac.ed.acp.cw2.data;

import org.slf4j.Logger;
import uk.ac.ed.acp.cw2.controller.ServiceController;

/**
 * Utility class for handling angle-related error handling.
 */
public class Angle {

    public static Boolean errorHandler(Double degrees) {
        Logger logger = ServiceController.getLogger();

        // check if degrees is null
        if (degrees == null) {
            if (ServiceController.VERBOSE) {
                logger.error("degrees is null");
            } return true;
        }

        // Check if degrees is out of bounds
        else if (degrees < 0 || degrees > 360) {
            if (ServiceController.VERBOSE) {
                logger.error("degrees out of bounds");
            } return true;
        }

        // Check if degrees is a multiple of 22.5, and therefore one of the 16 cardinal directions
        double sector = 22.5;
        if (degrees % sector != 0) {
            if (ServiceController.VERBOSE) {
                logger.error("degrees not a multiple of 22.5");
            } return true;
        } return false;
    }
}
