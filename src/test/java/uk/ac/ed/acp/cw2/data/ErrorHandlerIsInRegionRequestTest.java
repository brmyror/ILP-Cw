package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.IsInRegionRequest;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.dto.Region;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        LngLat pos = LngLat.builder().lng(0.0).lat(0.0).build();
        Region region = Region.builder().name("TestRegion")
                .vertices(new LngLat[]{
                        LngLat.builder().lng(-1.0).lat(-1.0).build(),
                        LngLat.builder().lng(1.0).lat(-1.0).build(),
                        LngLat.builder().lng(1.0).lat(1.0).build(),
                        LngLat.builder().lng(-1.0).lat(-1.0).build()
                })
                .build();
        var req = IsInRegionRequest.builder().lngLat(pos).region(region).build();
        assertFalse(ErrorHandler.isInRegionRequest(req, LoggerFactory.getLogger("test")));
    }
}
