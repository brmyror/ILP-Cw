package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for ErrorHandler longitude/latitude validation
class ErrorHandlerLngLatRequestTest {

    // Test for out-of-bounds longitude and latitude
    @ParameterizedTest
    @CsvSource({"-181,0", "181,0", "0,-91", "0,91", "-200,95", "200,-95"})
    void LngLatOutOfBoundsTest(Double lng, Double lat) {
        var pos = LngLatRequest.builder().lng(lng).lat(lat).build();
        assertTrue(ErrorHandler.lngLatRequest(pos, org.slf4j.LoggerFactory.getLogger("test")));
    }

    // Test for null and NaN longitude and latitude
    @ParameterizedTest
    @CsvSource({"null,0", "0,null", "null, null", "NaN,0", "0,NaN", "NaN,NaN"})
    void LngLatNullOrNaNTest(String lngStr, String latStr) {
        Double lng = lngStr.equals("null") ? null : (lngStr.equals("NaN") ? Double.NaN : Double.parseDouble(lngStr));
        Double lat = latStr.equals("null") ? null : (latStr.equals("NaN") ? Double.NaN : Double.parseDouble(latStr));
        var pos = LngLatRequest.builder().lng(lng).lat(lat).build();
        assertTrue(ErrorHandler.lngLatRequest(pos, org.slf4j.LoggerFactory.getLogger("test")));
    }

    // Test for null LngLatRequest object
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void LngLatNullTest(Boolean isNull) {
        LngLatRequest pos = isNull ? null : LngLatRequest.builder().build();
        assertTrue(ErrorHandler.lngLatRequest(pos, org.slf4j.LoggerFactory.getLogger("test")));
    }

    // Test for valid longitude and latitude
    @ParameterizedTest
    @CsvSource({"-180,0", "180,0", "0,-90", "0,90", "45.5,22.3", "-73.987,40.733" })
    void LngLatValidTest(Double lng, Double lat) {
        var pos = LngLatRequest.builder().lng(lng).lat(lat).build();
        assertFalse(ErrorHandler.lngLatRequest(pos, LoggerFactory.getLogger("test")));
    }
}

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
    @CsvSource({"-180,0,180,0", "0,-90,0,90", "45.5,22.3,-73.987,40.733" })
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

// Unit tests for ErrorHandler angle validation
class ErrorHandlerAngleTest {

    // Test for out-of-bounds angles
    @ParameterizedTest
    @CsvSource({"-1", "361", "720", "-450"})
    void angleOutOfBoundsTest(Double degrees) {
        assertTrue(ErrorHandler.angle(degrees, LoggerFactory.getLogger("test")));
    }

    // Test for null angle
    @Test
    void angleNullTest() {
        assertTrue(ErrorHandler.angle(null, LoggerFactory.getLogger("test")));
    }

    // Test for NaN angle
    @Test
    void angleNaNTest() {
        assertTrue(ErrorHandler.angle(Double.NaN, LoggerFactory.getLogger("test")));
    }

    // Test for angles not a multiple of 22.5
    @ParameterizedTest
    @CsvSource({"10", "45.1", "100.3", "359.9"})
    void angleNotMultipleOf22_5Test(Double degrees) {
        assertTrue(ErrorHandler.angle(degrees, LoggerFactory.getLogger("test")));
    }

    // Test for valid angles
    @ParameterizedTest
    @CsvSource({"0", "22.5", "45", "67.5", "90", "112.5", "135", "157.5", "180", "202.5", "225", "247.5", "270", "292.5", "315", "337.5", "360"})
    void angleValidTest(Double degrees) {
        assertFalse(ErrorHandler.angle(degrees, LoggerFactory.getLogger("test")));
    }
}

// Unit tests for ErrorHandler next position request validation
class ErrorHandlerNextPositionRequestTest {

    // Test for null next position request
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void nextPositionNullTest(Boolean isNull) {
        NextPositionRequest req = isNull ? null : NextPositionRequest.builder().build();
        assertTrue(ErrorHandler.nextPositionRequest(req, LoggerFactory.getLogger("test")));
    }

    // Test for next position request with invalid start
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void nextPositionInvalidTest(Boolean isNull) {
        LngLatRequest start = isNull ? null : LngLatRequest.builder().build();
        var req = NextPositionRequest.builder().start(start).angle(0.0).build();
        assertTrue(ErrorHandler.nextPositionRequest(req, LoggerFactory.getLogger("test")));
    }

    // Test for next position request with valid start and angle
    @ParameterizedTest
    @CsvSource({"-73.987,40.733,45.0", "0.0,0.0,90.0", "180.0,-90.0,270.0"})
    void nextPositionValidTest(Double lng, Double lat, Double angle) {
        var start = LngLatRequest.builder().lng(lng).lat(lat).build();
        var req = NextPositionRequest.builder().start(start).angle(angle).build();
        assertFalse(ErrorHandler.nextPositionRequest(req, LoggerFactory.getLogger("test")));
    }
}

// Unit tests for ErrorHandler Region request validation
class ErrorHandlerRegionRequestTest {

    // Test for null region request
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void regionNullTest(Boolean isNull) {
        RegionRequest req = isNull ? null : RegionRequest.builder().build();
        assertTrue(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }

    // Test for region request with null name
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void regionInvalidNameTest(Boolean isNull) {
        String name = isNull ? null : "";
        var req = RegionRequest.builder().name(name).build();
        assertTrue(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }

    // Test for region request with null vertices
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void regionInvalidVerticesTest(Boolean isNull) {
        LngLatRequest[] vertices = isNull ? null : new LngLatRequest[] { LngLatRequest.builder().build() };
        var req = RegionRequest.builder().name("TestRegion").vertices(vertices).build();
        assertTrue(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }

    // Test for region request with less than 3 vertices
    @ParameterizedTest
    @CsvSource({"0", "1", "2"})
    void regionInsufficientVerticesTest(int vertexCount) {
        LngLatRequest[] vertices = new LngLatRequest[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            vertices[i] = LngLatRequest.builder().lng(0.0).lat(0.0).build();
        }
        var req = RegionRequest.builder().name("TestRegion").vertices(vertices).build();
        assertTrue(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }

    // Test for region request with non-closed polygon
    @Test
    void regionNonClosedPolygonTest() {
        LngLatRequest[] vertices = new LngLatRequest[] {
                LngLatRequest.builder().lng(-1.0).lat(-1.0).build(),
                LngLatRequest.builder().lng(1.0).lat(-1.0).build(),
                LngLatRequest.builder().lng(1.0).lat(1.0).build()
        };
        var req = RegionRequest.builder().name("TestRegion").vertices(vertices).build();
        assertTrue(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }

    // Test for region request with valid vertices
    @Test
    void regionValidTest() {
        LngLatRequest[] vertices = new LngLatRequest[] {
                LngLatRequest.builder().lng(-1.0).lat(-1.0).build(),
                LngLatRequest.builder().lng(1.0).lat(-1.0).build(),
                LngLatRequest.builder().lng(1.0).lat(1.0).build(),
                LngLatRequest.builder().lng(-1.0).lat(-1.0).build()
        };
        var req = RegionRequest.builder().name("TestRegion").vertices(vertices).build();
        assertFalse(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }
}

// Unit tests for ErrorHandler isInRegion request validation
class ErrorHandlerIsInRegionRequestTest {

    // Test for null isInRegion request
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void isInRegionNullTest(Boolean isNull) {
        IsInRegionRequest req = isNull ? null : IsInRegionRequest.builder().build();
        assertTrue(ErrorHandler.isInRegionRequest(req, LoggerFactory.getLogger("test")));
    }

    // Test for isInRegion request with valid position and region
    @Test
    void isInRegionValidTest() {
        LngLatRequest pos = LngLatRequest.builder().lng(0.0).lat(0.0).build();
        RegionRequest region = RegionRequest.builder().name("TestRegion")
                .vertices(new LngLatRequest[] {
                        LngLatRequest.builder().lng(-1.0).lat(-1.0).build(),
                        LngLatRequest.builder().lng(1.0).lat(-1.0).build(),
                        LngLatRequest.builder().lng(1.0).lat(1.0).build(),
                        LngLatRequest.builder().lng(-1.0).lat(-1.0).build()
                })
                .build();
        var req = IsInRegionRequest.builder().lngLatRequest(pos).region(region).build();
        assertFalse(ErrorHandler.isInRegionRequest(req, LoggerFactory.getLogger("test")));
    }
}
