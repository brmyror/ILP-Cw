package uk.ac.ed.acp.cw2.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
