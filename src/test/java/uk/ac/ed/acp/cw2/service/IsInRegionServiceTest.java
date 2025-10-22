package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.dto.IsInRegionRequest;
import uk.ac.ed.acp.cw2.dto.LngLatRequest;
import uk.ac.ed.acp.cw2.dto.RegionRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

// Unit tests for IsInRegionService
class IsInRegionServiceTest extends BaseServiceTest {

    // Test valid isInRegion service call where point is inside region
    @Test
    void pointInsideRegionReturnsTrue() {
        LngLatRequest pos = LngLatRequest.builder().lng(0.0).lat(0.0).build();
        LngLatRequest[] vertices = new LngLatRequest[]{
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
        LngLatRequest[] vertices = new LngLatRequest[]{
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
        LngLatRequest[] vertices = new LngLatRequest[]{
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
