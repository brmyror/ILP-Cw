package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.LngLat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for ErrorHandler longitude/latitude validation
class ErrorHandlerLngLatTest {

    // Test for out-of-bounds longitude and latitude
    @ParameterizedTest
    @CsvSource({"-181,0", "181,0", "0,-91", "0,91", "-200,95", "200,-95"})
    void LngLatOutOfBoundsTest(Double lng, Double lat) {
        var pos = LngLat.builder().lng(lng).lat(lat).build();
        assertTrue(ErrorHandler.lngLatRequest(pos, org.slf4j.LoggerFactory.getLogger("test")));
    }

    // Test for null and NaN longitude and latitude
    @ParameterizedTest
    @CsvSource({"null,0", "0,null", "null, null", "NaN,0", "0,NaN", "NaN,NaN"})
    void LngLatNullOrNaNTest(String lngStr, String latStr) {
        Double lng = lngStr.equals("null") ? null : (lngStr.equals("NaN") ? Double.NaN : Double.parseDouble(lngStr));
        Double lat = latStr.equals("null") ? null : (latStr.equals("NaN") ? Double.NaN : Double.parseDouble(latStr));
        var pos = LngLat.builder().lng(lng).lat(lat).build();
        assertTrue(ErrorHandler.lngLatRequest(pos, org.slf4j.LoggerFactory.getLogger("test")));
    }

    // Test for null LngLatRequest object
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void LngLatNullTest(Boolean isNull) {
        LngLat pos = isNull ? null : LngLat.builder().build();
        assertTrue(ErrorHandler.lngLatRequest(pos, org.slf4j.LoggerFactory.getLogger("test")));
    }

    // Test for valid longitude and latitude
    @ParameterizedTest
    @CsvSource({"-180,0", "180,0", "0,-90", "0,90", "45.5,22.3", "-73.987,40.733"})
    void LngLatValidTest(Double lng, Double lat) {
        var pos = LngLat.builder().lng(lng).lat(lat).build();
        assertFalse(ErrorHandler.lngLatRequest(pos, LoggerFactory.getLogger("test")));
    }
}
