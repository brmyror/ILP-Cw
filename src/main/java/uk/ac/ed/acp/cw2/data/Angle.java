package uk.ac.ed.acp.cw2.data;

/**
 * Utility class for handling angle-related error handling.
 */
public class Angle {

    public static Boolean errorHandler(Double degrees) {
        if (degrees < 0 || degrees > 360) return null;

        // handles the index overflow edge case
        if (degrees == 360) degrees = (double) 0;

        // Check if degrees is a multiple of 22.5, and therefore one of the 16 cardinal directions
        double sector = 22.5;
        if (degrees % sector != 0) return null;

        return true;
    }
}
