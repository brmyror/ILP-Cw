package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

