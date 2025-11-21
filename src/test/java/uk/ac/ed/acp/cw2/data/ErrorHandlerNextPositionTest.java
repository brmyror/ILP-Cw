package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.dto.NextPosition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for ErrorHandler next position request validation
class ErrorHandlerNextPositionTest {

    // Test for null next position request
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void nextPositionNullTest(Boolean isNull) {
        NextPosition req = isNull ? null : NextPosition.builder().build();
        assertTrue(ErrorHandler.nextPositionRequest(req, LoggerFactory.getLogger("test")));
    }

    // Test for next position request with invalid start
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void nextPositionInvalidTest(Boolean isNull) {
        LngLat start = isNull ? null : LngLat.builder().build();
        var req = NextPosition.builder().start(start).angle(0.0).build();
        assertTrue(ErrorHandler.nextPositionRequest(req, LoggerFactory.getLogger("test")));
    }

    // Test for next position request with valid start and angle
    @ParameterizedTest
    @CsvSource({"-73.987,40.733,45.0", "0.0,0.0,90.0", "180.0,-90.0,270.0"})
    void nextPositionValidTest(Double lng, Double lat, Double angle) {
        var start = LngLat.builder().lng(lng).lat(lat).build();
        var req = NextPosition.builder().start(start).angle(angle).build();
        assertFalse(ErrorHandler.nextPositionRequest(req, LoggerFactory.getLogger("test")));
    }
}
