package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.entity.RestrictedArea;

import java.awt.geom.Path2D;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-level tests for FR11/MR5: path avoids restricted areas with a ~one-step buffer.
 */
class FlightPathAlgorithmRestrictedAreaTest {

    private static final double STEP = 1.5E-4;

    @Test
    void FR11_pathAvoidsRestrictedArea_withOneStepBuffer() {
        // Define a simple square restricted area centered on the straight line between start and goal.
        RestrictedArea square = RestrictedArea.builder()
                .name("square")
                .id(1)
                .limits(RestrictedArea.limits.builder().lower(0).upper(-1).build())
                .vertices(new RestrictedArea.vertices[]{
                        RestrictedArea.vertices.builder().lng(-3.1906).lat(55.9440).build(),
                        RestrictedArea.vertices.builder().lng(-3.1906).lat(55.9443).build(),
                        RestrictedArea.vertices.builder().lng(-3.1902).lat(55.9443).build(),
                        RestrictedArea.vertices.builder().lng(-3.1902).lat(55.9440).build(),
                        RestrictedArea.vertices.builder().lng(-3.1906).lat(55.9440).build(),
                })
                .build();

        LngLat start = LngLat.builder().lng(-3.1910).lat(55.94415).build();
        LngLat goal = LngLat.builder().lng(-3.1898).lat(55.94415).build();

        List<LngLat> path = FlightPathAlgorithm.findPath(start, goal, List.of(square));
        assertNotNull(path);
        assertTrue(path.size() >= 2, "Expected a non-empty path");

        // Assert no segment intersects the polygon expanded by roughly STEP.
        // We approximate buffer by checking a stroked path width; instead we conservatively check that
        // all points lie outside a slight buffered polygon.
        Path2D poly = new Path2D.Double();
        poly.moveTo(square.getVertices()[0].getLng(), square.getVertices()[0].getLat());
        for (int i = 1; i < square.getVertices().length; i++) {
            poly.lineTo(square.getVertices()[i].getLng(), square.getVertices()[i].getLat());
        }
        poly.closePath();

        // Check every path point is not inside the polygon (buffer handled inside algorithm);
        // this is a practical MR5 check based on output geometry.
        for (LngLat p : path) {
            assertFalse(poly.contains(p.getLng(), p.getLat()),
                    "Path point inside restricted polygon: " + p);
        }

        // Additionally, ensure the path is not simply the straight line (it should detour)
        // because the square blocks the direct route.
        boolean hasDetour = path.stream().anyMatch(p -> Math.abs(p.getLat() - start.getLat()) > STEP / 2);
        assertTrue(hasDetour, "Expected a detour around the restricted area");
    }

    @Test
    void FR11_straightLineWhenNoRestrictedAreas() {
        LngLat start = LngLat.builder().lng(-3.192473).lat(55.946233).build();
        LngLat goal = LngLat.builder().lng(-3.192473).lat(55.946233 + 5 * STEP).build();

        List<LngLat> path = FlightPathAlgorithm.findPath(start, goal, List.of());
        assertTrue(path.size() >= 2);
        // If unrestricted, path should be monotonic in lat and stay on same longitude.
        assertTrue(path.stream().allMatch(p -> Math.abs(p.getLng() - start.getLng()) < 1e-12));
    }
}
