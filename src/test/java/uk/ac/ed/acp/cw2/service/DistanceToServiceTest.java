package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.dto.PositionPair;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// Unit tests for DistanceToService
class DistanceToServiceTest extends BaseServiceTest {

    // Test valid distance service call
    @Test
    void returnsDistanceAnd200() {
        var p1 = LngLat.builder().lng(-3.192473).lat(55.946233).build();
        var p2 = LngLat.builder().lng(-3.192473).lat(55.946300).build();
        var req = PositionPair.builder().lngLat1(p1).lngLat2(p2).build();

        Double distance = DistanceToService.distanceTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(distance);
        assertTrue(distance > 0);
    }

    // Test distance service call with identical positions
    @Test
    void identicalPositionsReturnZeroDistance() {
        var p1 = LngLat.builder().lng(-3.192473).lat(55.946233).build();
        var p2 = LngLat.builder().lng(-3.192473).lat(55.946233).build();
        var req = PositionPair.builder().lngLat1(p1).lngLat2(p2).build();

        Double distance = DistanceToService.distanceTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(distance);
        assertEquals(0.0, distance);
    }

    // Test distance service call with large distance
    @Test
    void largeDistanceCalculation() {
        var p1 = LngLat.builder().lng(-100.0).lat(80.0).build();
        var p2 = LngLat.builder().lng(100.0).lat(-80.0).build();
        var req = PositionPair.builder().lngLat1(p1).lngLat2(p2).build();

        Double distance = DistanceToService.distanceTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(distance);
        double expectedDistance = Math.sqrt(Math.pow(80.0 - (-80.0), 2) + Math.pow(-100.0 - 100.0, 2));
        assertEquals(expectedDistance, distance, 1e-12);
    }

    // Test distance service calls for symmetry
    @Test
    void distanceIsSymmetric() {
        var p1 = LngLat.builder().lng(-3.192473).lat(55.946233).build();
        var p2 = LngLat.builder().lng(-3.192500).lat(55.946300).build();
        var req1 = PositionPair.builder().lngLat1(p1).lngLat2(p2).build();
        var req2 = PositionPair.builder().lngLat1(p2).lngLat2(p1).build();

        Double distance1 = DistanceToService.distanceTo(req1, response, logger);
        Double distance2 = DistanceToService.distanceTo(req2, response, logger);

        verify(response, times(2)).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(distance1);
        assertNotNull(distance2);
        assertEquals(distance1, distance2, 1e-12);
    }

    // Test invalid distance service call
    @Test
    void invalidDistanceRequestReturns400AndNull() {
        var p1 = LngLat.builder().build(); // both positions null
        var p2 = LngLat.builder().build();
        var req = PositionPair.builder().lngLat1(p1).lngLat2(p2).build();

        Double distance = DistanceToService.distanceTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(distance);
    }
}
