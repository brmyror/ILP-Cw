package uk.ac.ed.acp.cw2.data;

import org.slf4j.Logger;
import uk.ac.ed.acp.cw2.dto.*;

/**
 * ErrorHandler class containing static methods to validate various request objects.
 */
public class ErrorHandler {

    private static final Boolean VERBOSE = true;

    // Error handler for Position objects
    public static Boolean lngLatRequest(LngLat pos, Logger logger) {

        // Check if pos is null
        if (pos == null) {
            if (VERBOSE) {
                logger.error("Position itself null");
            } return true;
        }

        // Check if lng or lat is null
        else if (pos.getLng() == null || pos.getLat() == null) {
            if (VERBOSE) {
                logger.error("Longitude or Latitude null");
            } return true;
        }

        // Check if lng or lat is NaN
        else if (pos.getLng().isNaN() || pos.getLat().isNaN()) {
            if (VERBOSE) {
                logger.error("Longitude or Latitude is NaN");
            } return true;
        }

        // Longitude must be between -180 and 180,
        else if (pos.getLng() > 180 || pos.getLng() < -180) {
            if (VERBOSE) {
                logger.error("Longitude out of range");
            } return true;
        }

        // Latitude must be between -90 and 90
        else if (pos.getLat() > 90 || pos.getLat() < -90) {
            if (VERBOSE) {
                logger.error("Latitude out of range");
            } return true;
        } return false;
    }

    // Error handler for positionPairRequest objects
    public static Boolean positionPairRequest(PositionPair req, Logger logger) {
        // Check if req is null
        if (req == null) {
            if (VERBOSE) {
                logger.error("LngLatPairRequest itself null");
            } return true;
        }

        // Check if pos1 or pos2 has an error
        else if (lngLatRequest(req.getLngLat1(), logger)) {
            return true;
        } else return lngLatRequest(req.getLngLat2(), logger);
    }

    // Error handler for NextPosition objects
    public static Boolean nextPositionRequest(NextPosition req, Logger logger) {
        // Check if req is null
        if (req == null) {
            if (VERBOSE) {
                logger.error("NextPosition itself null");
            } return true;
        }

        // Check if start has an error
        else if (lngLatRequest(req.getStart(), logger)) {
            if (VERBOSE) {
                logger.error("Start position has error");
            } return true;
        }

        // Check if angle has an error
        else if (angle(req.getAngle(), logger)) {
            if (VERBOSE) {
                logger.error("Angle has error");
            } return true;
        } return false;
    }

    // Error handler for angle in degrees
    public static Boolean angle(Double degrees, Logger logger) {
        // check if degrees is null or NaN
        if (degrees == null || degrees.isNaN()) {
            if (VERBOSE) {
                logger.error("degrees is null");
            } return true;
        }

        // Check if degrees is out of bounds
        else if (degrees < 0 || degrees > 360) {
            if (VERBOSE) {
                logger.error("degrees out of bounds");
            } return true;
        }

        // Check if degrees is a multiple of 22.5, and therefore one of the 16 cardinal directions
        double sector = 22.5;
        if (degrees % sector != 0) {
            if (VERBOSE) {
                logger.error("degrees not a multiple of 22.5");
            } return true;
        } return false;
    }

    // Error handler for IsInRegion objects
    public static Boolean isInRegionRequest(IsInRegion req, Logger logger) {
        // Check if req is null
        if (req == null ) {
            if (VERBOSE) {
                logger.error("IsInRegion itself null");
            } return true;
        }

        // Check if position has errors
        else if (lngLatRequest(req.getLngLat(), logger)) {
            return true;
        }
        //return false;
        // Check if region has errors
        else return region(req.getRegion(), logger);
    }

    // Error handler for Region objects
    public static Boolean region(Region region, Logger logger) {
        // Check if region is null
        if (region == null) {
            if (VERBOSE) {
                logger.error("Region itself null");
            } return true;
        }

        // Check if name is null or blank
        else if (region.getName() == null || region.getName().isEmpty()) {
            if (VERBOSE) {
                logger.error("Region name null or blank");
            } return true;
        }

        // Check if vertices is null
        else if (region.getVertices() == null) {
            if (VERBOSE) {
                logger.error("Region vertices null");
            } return true;
        }

        // Check if vertices has less than 4 positions as region must be a closed polygon (triangle and closing point)
        else if (region.getVertices().length < 4) {
            if (VERBOSE) {
                logger.error("Region vertices has less than 4 positions");
            } return true;
        }

        // Check if the first and last positions exist and are the same (closed polygon)
        LngLat first = region.getVertices()[0];
        LngLat last = region.getVertices()[region.getVertices().length - 1];
        if (first == null || last == null) {
            if (VERBOSE) {
                logger.error("First or last position in vertices is null");
            } return true;
        }
        if (!first.getLat().equals(last.getLat()) || !first.getLng().equals(last.getLng())) {
            if (VERBOSE) {
                logger.error("First and last position in vertices are not the same");
            } return true;
        }

        // Check if any position in vertices has an error
        int index = 1;
        for (LngLat pos : region.getVertices()) {
            if (lngLatRequest(pos, logger)) {
                if (VERBOSE) {
                    logger.error("Position {} in vertices has an error", index);
                } return true;
            } index ++;
        }
        return false;
    }
}
