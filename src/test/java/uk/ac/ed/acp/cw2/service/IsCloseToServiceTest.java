package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.dto.LngLatRequest;
import uk.ac.ed.acp.cw2.dto.PositionPairRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

// Unit tests for IsCloseToService
class IsCloseToServiceTest extends BaseServiceTest {

    // Test valid and true isCloseTo service call where distance < 0.00015
    @Test
    void belowDistanceThreshold() {
        var p1 = LngLatRequest.builder().lng(0.0).lat(0.0).build();
        var p2 = LngLatRequest.builder().lng(0.0001499).lat(0.0).build();
        var req = PositionPairRequest.builder().lngLatRequest1(p1).lngLatRequest2(p2).build();

        Boolean isClose = IsCloseToService.isCloseTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(isClose);
        assertTrue(isClose);
    }

    // Test valid and false isCloseTo service call where distance == 0.00015
    @Test
    void atDistanceThreshold() {
        var p1 = LngLatRequest.builder().lng(0.0).lat(0.0).build();
        var p2 = LngLatRequest.builder().lng(0.00015).lat(0.0).build();
        var req = PositionPairRequest.builder().lngLatRequest1(p1).lngLatRequest2(p2).build();

        Boolean isClose = IsCloseToService.isCloseTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(isClose);
        assertFalse(isClose);
    }

    // Test valid and false isCloseTo service call where distance > 0.00015
    @Test
    void aboveDistanceThreshold() {
        var p1 = LngLatRequest.builder().lng(0.0).lat(0.0).build();
        var p2 = LngLatRequest.builder().lng(0.000151).lat(0.0).build();
        var req = PositionPairRequest.builder().lngLatRequest1(p1).lngLatRequest2(p2).build();

        Boolean isClose = IsCloseToService.isCloseTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(isClose);
        assertFalse(isClose);
    }

    // Test invalid isCloseTo service call
    @Test
    void invalidIsCloseToRequestReturns400AndNull() {
        var p1 = LngLatRequest.builder().build(); // both positions null
        var p2 = LngLatRequest.builder().build();
        var req = PositionPairRequest.builder().lngLatRequest1(p1).lngLatRequest2(p2).build();

        Boolean isClose = IsCloseToService.isCloseTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(isClose);
    }
}
