package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.dto.NextPositionRequest;
import uk.ac.ed.acp.cw2.dto.PositionPair;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit-level precision tests for MR2 (results correct to ~5-6 significant figures).
 */
class DistanceAndNextPositionPrecisionTest extends BaseServiceTest {

    private static final double STEP = 1.5E-4;

    @Test
    void FR2_MR2_distanceTo_isAccurateTo6DecimalPlaces() {
        var p1 = LngLat.builder().lng(-3.192473).lat(55.946233).build();
        var p2 = LngLat.builder().lng(-3.192473).lat(55.946233 + STEP).build();
        var req = PositionPair.builder().lngLat1(p1).lngLat2(p2).build();

        Double dist = DistanceToService.distanceTo(req, response, logger);
        verify(response).setStatus(HttpServletResponse.SC_OK);

        assertNotNull(dist);
        // Movement is exactly STEP in latitude, so Euclidean distance should be STEP.
        assertEquals(STEP, dist, 1e-9);
    }

    @Test
    void FR4_MR2_nextPosition_stepSizeIsCorrect_forCardinalDirection() {
        var start = LngLat.builder().lng(-3.0).lat(55.0).build();

        var req0 = NextPositionRequest.builder().start(start).angle(0.0).build();
        LngLat p0 = NextPositionService.nextPosition(req0, response, logger);
        assertNotNull(p0);
        assertEquals(start.getLat(), p0.getLat(), 1e-12);
        assertEquals(start.getLng() + STEP, p0.getLng(), 1e-9);

        var req90 = NextPositionRequest.builder().start(start).angle(90.0).build();
        LngLat p90 = NextPositionService.nextPosition(req90, response, logger);
        assertNotNull(p90);
        assertEquals(start.getLng(), p90.getLng(), 1e-12);
        assertEquals(start.getLat() + STEP, p90.getLat(), 1e-9);

        // Two calls => status should be set twice
        verify(response, times(2)).setStatus(HttpServletResponse.SC_OK);
    }
}
