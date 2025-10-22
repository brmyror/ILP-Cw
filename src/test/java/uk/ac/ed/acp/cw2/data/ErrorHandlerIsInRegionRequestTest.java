package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import uk.ac.ed.acp.cw2.dto.IsInRegionRequest;
import uk.ac.ed.acp.cw2.dto.LngLatRequest;
import uk.ac.ed.acp.cw2.dto.RegionRequest;

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
        LngLatRequest pos = LngLatRequest.builder().lng(0.0).lat(0.0).build();
        RegionRequest region = RegionRequest.builder().name("TestRegion")
                .vertices(new LngLatRequest[]{
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
