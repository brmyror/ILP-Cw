package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import uk.ac.ed.acp.cw2.dto.LngLatRequest;
import uk.ac.ed.acp.cw2.dto.NextPositionRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

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
