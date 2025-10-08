package uk.ac.ed.acp.cw2.data;

/**
 * Enum representing compass angles in 22.5-degree increments.
 * Is entirely unneeded but makes the code more readable and will possibly be used in CW2, and have ended up
 * using it for handling proper angles in the NextPosition endpoint.
 */
public enum Angle {
    NORTH, NORTH_NORTH_EAST, NORTH_EAST, EAST_NORTH_EAST, EAST, EAST_SOUTH_EAST, SOUTH_EAST, SOUTH_SOUTH_EAST,
    SOUTH, SOUTH_SOUTH_WEST, SOUTH_WEST, WEST_SOUTH_WEST, WEST, WEST_NORTH_WEST, NORTH_WEST, NORTH_NORTH_WEST;

    public static Angle fromDegrees(Double degrees) {
        if (degrees < 0 || degrees > 360) return null;

        // handles the index overflow edge case
        if (degrees == 360) degrees = (double) 0;

        // Check if degrees is a multiple of 22.5, and therefore one of the 16 cardinal directions
        double sector = 22.5;
        if (degrees % sector != 0) return null;

        int index = (int) (degrees / sector);
        return Angle.values()[index];
    }
}
