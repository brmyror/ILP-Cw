package uk.ac.ed.acp.cw2.data;

import uk.ac.ed.acp.cw2.dto.LngLatRequest;

/**
 * Utility class for calculating distances between two positions.
 */
public class Distance {
    public static double calculateEuclideanDistance(LngLatRequest p1, LngLatRequest p2) {
        double dx = p1.getLat() - p2.getLat();
        double dy = p1.getLng() - p2.getLng();
        return Math.sqrt((dx * dx) + (dy * dy));
    }
}
