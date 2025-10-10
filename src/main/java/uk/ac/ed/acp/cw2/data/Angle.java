package uk.ac.ed.acp.cw2.data;

/**
 * Utility class for handling angle-related error handling.
 */
public class Angle {

    public static Boolean errorHandler(Double degrees) {
        // check if degrees is null or out of bounds
        if (degrees == null) return true;
        else if (degrees < 0 || degrees > 360) return true;

        // handles the overflow edge case
        else if (degrees == 360) degrees = (double) 0;

        // Check if degrees is a multiple of 22.5, and therefore one of the 16 cardinal directions
        double sector = 22.5;
        return degrees % sector != 0;
    }
}
