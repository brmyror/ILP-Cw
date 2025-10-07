package uk.ac.ed.acp.cw2.data;

/**
 * Enum representing compass angles in 22.5-degree increments.
 */
public enum Angle {
    NORTH, NORTH_NORTH_EAST, NORTH_EAST, EAST_NORTH_EAST, EAST, EAST_SOUTH_EAST, SOUTH_EAST, SOUTH_SOUTH_EAST,
    SOUTH, SOUTH_SOUTH_WEST, SOUTH_WEST, WEST_SOUTH_WEST, WEST, WEST_NORTH_WEST, NORTH_WEST, NORTH_NORTH_WEST;

    public static Angle fromDegrees(double degrees) {
        if (degrees < 0 || degrees > 360) return null;


        double sector = 22.5;
        if (degrees % sector != 0) return null;

        // handles the index overflow edge case
        if (degrees == 360) degrees = 0;

        int index = (int) (degrees / sector);
        return Angle.values()[index];
    }
}
