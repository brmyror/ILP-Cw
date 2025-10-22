package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import uk.ac.ed.acp.cw2.dto.*;

// Base test class to set up common mocks
class BaseServiceTest {
    protected HttpServletResponse response;
    protected Logger logger;

    @BeforeEach
    void setUp() {
        response = mock(HttpServletResponse.class);
        logger = mock(Logger.class);
    }
}

// Unit tests for DistanceToService
class DistanceToServiceTest extends BaseServiceTest {

    // Test valid distance service call
    @Test
    void returnsDistanceAnd200() {
        var p1 = LngLatRequest.builder().lng(-3.192473).lat(55.946233).build();
        var p2 = LngLatRequest.builder().lng(-3.192473).lat(55.946300).build();
        var req = PositionPairRequest.builder().lngLatRequest1(p1).lngLatRequest2(p2).build();

        Double distance = DistanceToService.distanceTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(distance);
        assertTrue(distance > 0);
    }

    // Test distance service call with identical positions
    @Test
    void identicalPositionsReturnZeroDistance() {
        var p1 = LngLatRequest.builder().lng(-3.192473).lat(55.946233).build();
        var p2 = LngLatRequest.builder().lng(-3.192473).lat(55.946233).build();
        var req = PositionPairRequest.builder().lngLatRequest1(p1).lngLatRequest2(p2).build();

        Double distance = DistanceToService.distanceTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(distance);
        assertEquals(0.0, distance);
    }

    // Test distance service call with large distance
    @Test
    void largeDistanceCalculation() {
        var p1 = LngLatRequest.builder().lng(-100.0).lat(80.0).build();
        var p2 = LngLatRequest.builder().lng(100.0).lat(-80.0).build();
        var req = PositionPairRequest.builder().lngLatRequest1(p1).lngLatRequest2(p2).build();

        Double distance = DistanceToService.distanceTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(distance);
        double expectedDistance = Math.sqrt(Math.pow(80.0 - (-80.0), 2) + Math.pow(-100.0 - 100.0, 2));
        assertEquals(expectedDistance, distance, 1e-12);
    }

    // Test distance service calls for symmetry
    @Test
    void distanceIsSymmetric() {
        var p1 = LngLatRequest.builder().lng(-3.192473).lat(55.946233).build();
        var p2 = LngLatRequest.builder().lng(-3.192500).lat(55.946300).build();
        var req1 = PositionPairRequest.builder().lngLatRequest1(p1).lngLatRequest2(p2).build();
        var req2 = PositionPairRequest.builder().lngLatRequest1(p2).lngLatRequest2(p1).build();

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
        var p1 = LngLatRequest.builder().build(); // both positions null
        var p2 = LngLatRequest.builder().build();
        var req = PositionPairRequest.builder().lngLatRequest1(p1).lngLatRequest2(p2).build();

        Double distance = DistanceToService.distanceTo(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(distance);
    }
}

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

// Unit tests for NextPositionService
class NextPositionServiceTest extends BaseServiceTest {

    // Test valid nextPosition service call
    @Test
    void validNextPositionRequestReturns200AndNewPosition() {
        var start = LngLatRequest.builder().lng(-3.192473).lat(55.946233).build();
        var req = NextPositionRequest.builder().start(start).angle(90.0).build();

        LngLatRequest nextPosition = NextPositionService.nextPosition(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(nextPosition);
        assertEquals(start.getLng(), nextPosition.getLng(), 0.00001);
        assertTrue(nextPosition.getLat() > start.getLat());
    }

    // Test invalid nextPosition service call
    @Test
    void invalidNextPositionRequestReturns400AndNull() {
        var start = LngLatRequest.builder().build(); // invalid start position
        var req = NextPositionRequest.builder().start(start).angle(Double.NaN).build();

        LngLatRequest newPosition = NextPositionService.nextPosition(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(newPosition);
    }
}

// Unit tests for IsInRegionService
class IsInRegionServiceTest extends BaseServiceTest {

    // Test valid isInRegion service call where point is inside region
    @Test
    void pointInsideRegionReturnsTrue() {
        LngLatRequest pos = LngLatRequest.builder().lng(0.0).lat(0.0).build();
        LngLatRequest[] vertices = new LngLatRequest[] {
                LngLatRequest.builder().lng(-1.0).lat(-1.0).build(),
                LngLatRequest.builder().lng(1.0).lat(-1.0).build(),
                LngLatRequest.builder().lng(1.0).lat(1.0).build(),
                LngLatRequest.builder().lng(-1.0).lat(-1.0).build()
        };
        var region = RegionRequest.builder().name("TestRegion").vertices(vertices).build();

        var req = IsInRegionRequest.builder().lngLatRequest(pos).region(region).build();

        Boolean isInRegion = IsInRegionService.isInRegion(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(isInRegion);
        assertTrue(isInRegion);
    }

    // Test valid isInRegion service call where point is on edge of region
    @Test
    void pointOnEdgeOfRegionReturnsTrue() {
        LngLatRequest pos = LngLatRequest.builder().lng(-1.0).lat(-1.0).build();
        LngLatRequest[] vertices = new LngLatRequest[] {
                LngLatRequest.builder().lng(-1.0).lat(-1.0).build(),
                LngLatRequest.builder().lng(1.0).lat(-1.0).build(),
                LngLatRequest.builder().lng(1.0).lat(1.0).build(),
                LngLatRequest.builder().lng(-1.0).lat(-1.0).build()
        };
        var region = RegionRequest.builder().name("TestRegion").vertices(vertices).build();

        var req = IsInRegionRequest.builder().lngLatRequest(pos).region(region).build();

        Boolean isInRegion = IsInRegionService.isInRegion(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(isInRegion);
        assertTrue(isInRegion);
    }

    // Test valid isInRegion service call where point is outside region
    @Test
    void pointOutsideRegionReturnsFalse() {
        LngLatRequest pos = LngLatRequest.builder().lng(2.0).lat(2.0).build();
        LngLatRequest[] vertices = new LngLatRequest[] {
                LngLatRequest.builder().lng(-1.0).lat(-1.0).build(),
                LngLatRequest.builder().lng(1.0).lat(-1.0).build(),
                LngLatRequest.builder().lng(1.0).lat(1.0).build(),
                LngLatRequest.builder().lng(-1.0).lat(-1.0).build()
        };
        var region = RegionRequest.builder().name("TestRegion").vertices(vertices).build();

        var req = IsInRegionRequest.builder().lngLatRequest(pos).region(region).build();

        Boolean isInRegion = IsInRegionService.isInRegion(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(isInRegion);
        assertFalse(isInRegion);
    }

    // Test invalid isInRegion service call
    @Test
    void invalidIsInRegionRequestReturns400AndNull() {
        LngLatRequest pos = LngLatRequest.builder().build(); // invalid position
        var region = RegionRequest.builder().name("TestRegion").vertices(new LngLatRequest[0]).build();

        var req = IsInRegionRequest.builder().lngLatRequest(pos).region(region).build();

        Boolean isInRegion = IsInRegionService.isInRegion(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(isInRegion);
    }
}