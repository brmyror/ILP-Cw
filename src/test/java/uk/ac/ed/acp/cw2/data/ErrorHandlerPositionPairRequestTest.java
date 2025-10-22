package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.LngLatRequest;
import uk.ac.ed.acp.cw2.dto.PositionPairRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for ErrorHandler position pair validation
class ErrorHandlerPositionPairRequestTest {
    // Test for null position pair request
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void positionPairNullTest(Boolean isNull) {
        PositionPairRequest req = isNull ? null : PositionPairRequest.builder().build();
        assertTrue(ErrorHandler.positionPairRequest(req, LoggerFactory.getLogger("test")));
    }

    // Test for position pair request with invalid positions
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void positionPairInvalidTest(Boolean isNull) {
        LngLatRequest pos1 = isNull ? null : LngLatRequest.builder().build();
        LngLatRequest pos2 = isNull ? null : LngLatRequest.builder().build();
        var req = PositionPairRequest.builder()
                .lngLatRequest1(pos1)
                .lngLatRequest2(pos2)
                .build();
        assertTrue(ErrorHandler.positionPairRequest(req, LoggerFactory.getLogger("test")));
    }

    // Test for position pair request with valid positions
    @ParameterizedTest
    @CsvSource({"-180,0,180,0", "0,-90,0,90", "45.5,22.3,-73.987,40.733"})
    void positionPairValidTest(Double lng1, Double lat1, Double lng2, Double lat2) {
        var pos1 = LngLatRequest.builder().lng(lng1).lat(lat1).build();
        var pos2 = LngLatRequest.builder().lng(lng2).lat(lat2).build();
        var req = PositionPairRequest.builder()
                .lngLatRequest1(pos1)
                .lngLatRequest2(pos2)
                .build();
        assertFalse(ErrorHandler.positionPairRequest(req, LoggerFactory.getLogger("test")));
    }
}
