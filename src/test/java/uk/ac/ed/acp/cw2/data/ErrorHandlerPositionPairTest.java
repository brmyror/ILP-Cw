package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.dto.PositionPair;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for ErrorHandler position pair validation
class ErrorHandlerPositionPairTest {
    // Test for null position pair request
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void positionPairNullTest(Boolean isNull) {
        PositionPair req = isNull ? null : PositionPair.builder().build();
        assertTrue(ErrorHandler.positionPairRequest(req, LoggerFactory.getLogger("test")));
    }

    // Test for position pair request with invalid positions
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void positionPairInvalidTest(Boolean isNull) {
        LngLat pos1 = isNull ? null : LngLat.builder().build();
        LngLat pos2 = isNull ? null : LngLat.builder().build();
        var req = PositionPair.builder()
                .lngLat1(pos1)
                .lngLat2(pos2)
                .build();
        assertTrue(ErrorHandler.positionPairRequest(req, LoggerFactory.getLogger("test")));
    }

    // Test for position pair request with valid positions
    @ParameterizedTest
    @CsvSource({"-180,0,180,0", "0,-90,0,90", "45.5,22.3,-73.987,40.733"})
    void positionPairValidTest(Double lng1, Double lat1, Double lng2, Double lat2) {
        var pos1 = LngLat.builder().lng(lng1).lat(lat1).build();
        var pos2 = LngLat.builder().lng(lng2).lat(lat2).build();
        var req = PositionPair.builder()
                .lngLat1(pos1)
                .lngLat2(pos2)
                .build();
        assertFalse(ErrorHandler.positionPairRequest(req, LoggerFactory.getLogger("test")));
    }
}
