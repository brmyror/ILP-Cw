package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.dto.IsInRegionRequest;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.dto.Region;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

// Unit tests for IsInRegionService
class IsInRegionRequestServiceTest extends BaseServiceTest {

    // Test valid isInRegion service call where point is inside region
    @Test
    void pointInsideRegionReturnsTrue() {
        LngLat pos = LngLat.builder().lng(0.0).lat(0.0).build();
        LngLat[] vertices = new LngLat[]{
                LngLat.builder().lng(-1.0).lat(-1.0).build(),
                LngLat.builder().lng(1.0).lat(-1.0).build(),
                LngLat.builder().lng(1.0).lat(1.0).build(),
                LngLat.builder().lng(-1.0).lat(-1.0).build()
        };
        var region = Region.builder().name("TestRegion").vertices(vertices).build();

        var req = IsInRegionRequest.builder().lngLat(pos).region(region).build();

        Boolean isInRegion = IsInRegionService.isInRegion(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(isInRegion);
        assertTrue(isInRegion);
    }

    // Test valid isInRegion service call where point is on edge of region
    @Test
    void pointOnEdgeOfRegionReturnsTrue() {
        LngLat pos = LngLat.builder().lng(-1.0).lat(-1.0).build();
        LngLat[] vertices = new LngLat[]{
                LngLat.builder().lng(-1.0).lat(-1.0).build(),
                LngLat.builder().lng(1.0).lat(-1.0).build(),
                LngLat.builder().lng(1.0).lat(1.0).build(),
                LngLat.builder().lng(-1.0).lat(-1.0).build()
        };
        var region = Region.builder().name("TestRegion").vertices(vertices).build();

        var req = IsInRegionRequest.builder().lngLat(pos).region(region).build();

        Boolean isInRegion = IsInRegionService.isInRegion(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(isInRegion);
        assertTrue(isInRegion);
    }

    // Test valid isInRegion service call where point is at vertex of region
    @Test
    void pointAtVertexOfRegionReturnsTrue() {
        LngLat pos = LngLat.builder().lng(-3.192473).lat(55.946233).build();
        LngLat[] vertices = new LngLat[]{
                LngLat.builder().lng(-3.192473).lat(55.946233).build(),
                LngLat.builder().lng(-3.192473).lat(55.942617).build(),
                LngLat.builder().lng(-3.184319).lat(55.942617).build(),
                LngLat.builder().lng(-3.184319).lat(55.946233).build(),
                LngLat.builder().lng(-3.192473).lat(55.946233).build()
        };
        var region = Region.builder().name("TestRegion").vertices(vertices).build();

        var req = IsInRegionRequest.builder().lngLat(pos).region(region).build();

        Boolean isInRegion = IsInRegionService.isInRegion(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(isInRegion);
        assertTrue(isInRegion);
    }

    // Test valid isInRegion service call where point is outside region
    @Test
    void pointOutsideRegionReturnsFalse() {
        LngLat pos = LngLat.builder().lng(2.0).lat(2.0).build();
        LngLat[] vertices = new LngLat[]{
                LngLat.builder().lng(-1.0).lat(-1.0).build(),
                LngLat.builder().lng(1.0).lat(-1.0).build(),
                LngLat.builder().lng(1.0).lat(1.0).build(),
                LngLat.builder().lng(-1.0).lat(-1.0).build()
        };
        var region = Region.builder().name("TestRegion").vertices(vertices).build();

        var req = IsInRegionRequest.builder().lngLat(pos).region(region).build();

        Boolean isInRegion = IsInRegionService.isInRegion(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertNotNull(isInRegion);
        assertFalse(isInRegion);
    }

    // Test invalid isInRegion service call
    @Test
    void invalidIsInRegionRequestReturns400AndNull() {
        LngLat pos = LngLat.builder().build(); // invalid position
        var region = Region.builder().name("TestRegion").vertices(new LngLat[0]).build();

        var req = IsInRegionRequest.builder().lngLat(pos).region(region).build();

        Boolean isInRegion = IsInRegionService.isInRegion(req, response, logger);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertNull(isInRegion);
    }
}
