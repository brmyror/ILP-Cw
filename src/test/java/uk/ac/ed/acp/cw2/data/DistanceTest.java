package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.dto.LngLat;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Unit tests for Distance calculation
class DistanceTest {
    // Test for small difference in coordinates
    @Test
    void smallDifferenceDistanceTest() {
        var p1 = LngLat.builder()
                .lng(-3.0)
                .lat(55.0)
                .build();
        var p2 = LngLat.builder()
                .lng(-3.0001)
                .lat(55.0002)
                .build();
        double dx = 55 - 55.0002; // -0.0002
        double dy = -3.0 - (-3.0001); // 0.0001
        double expected = Math.sqrt(dx * dx + dy * dy);
        assertEquals(expected, Distance.calculateEuclideanDistance(p1, p2), 1e-12);
    }
    // Test for zero difference in coordinates
    @Test
    void zeroDifferenceDistanceTest() {
        var p1 = LngLat.builder()
                .lng(0.0)
                .lat(0.0)
                .build();
        var p2 = LngLat.builder()
                .lng(0.0)
                .lat(0.0)
                .build();
        double expected = 0.0;
        assertEquals(expected, Distance.calculateEuclideanDistance(p1, p2), 1e-12);
    }
    // Test for large difference in coordinates
    @Test
    void largeDifferenceDistanceTest() {
        var p1 = LngLat.builder()
                .lng(-100.0)
                .lat(80.0)
                .build();
        var p2 = LngLat.builder()
                .lng(100.0)
                .lat(-80.0)
                .build();
        double dx = 80.0 - (-80.0); // 160.0
        double dy = -100.0 - 100.0; // -200.0
        double expected = Math.sqrt(dx * dx + dy * dy);
        assertEquals(expected, Distance.calculateEuclideanDistance(p1, p2), 1e-12);
    }
}
