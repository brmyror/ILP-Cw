package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for ErrorHandler Region request validation
class ErrorHandlerRegionTest {

    // Test for null region request
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void regionNullTest(Boolean isNull) {
        Region req = isNull ? null : Region.builder().build();
        assertTrue(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }

    // Test for region request with null name
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void regionInvalidNameTest(Boolean isNull) {
        String name = isNull ? null : "";
        var req = Region.builder().name(name).build();
        assertTrue(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }

    // Test for region request with null vertices
    @ParameterizedTest
    @CsvSource({"true", "false"})
    void regionInvalidVerticesTest(Boolean isNull) {
        LngLat[] vertices = isNull ? null : new LngLat[] { LngLat.builder().build() };
        var req = Region.builder().name("TestRegion").vertices(vertices).build();
        assertTrue(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }

    // Test for region request with less than 3 vertices
    @ParameterizedTest
    @CsvSource({"0", "1", "2"})
    void regionInsufficientVerticesTest(int vertexCount) {
        LngLat[] vertices = new LngLat[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            vertices[i] = LngLat.builder().lng(0.0).lat(0.0).build();
        }
        var req = Region.builder().name("TestRegion").vertices(vertices).build();
        assertTrue(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }

    // Test for region request with non-closed polygon
    @Test
    void regionNonClosedPolygonTest() {
        LngLat[] vertices = new LngLat[] {
                LngLat.builder().lng(-1.0).lat(-1.0).build(),
                LngLat.builder().lng(1.0).lat(-1.0).build(),
                LngLat.builder().lng(1.0).lat(1.0).build()
        };
        var req = Region.builder().name("TestRegion").vertices(vertices).build();
        assertTrue(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }

    // Test for region request with valid vertices
    @Test
    void regionValidTest() {
        LngLat[] vertices = new LngLat[] {
                LngLat.builder().lng(-1.0).lat(-1.0).build(),
                LngLat.builder().lng(1.0).lat(-1.0).build(),
                LngLat.builder().lng(1.0).lat(1.0).build(),
                LngLat.builder().lng(-1.0).lat(-1.0).build()
        };
        var req = Region.builder().name("TestRegion").vertices(vertices).build();
        assertFalse(ErrorHandler.region(req, LoggerFactory.getLogger("test")));
    }
}

