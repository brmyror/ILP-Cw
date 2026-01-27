package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;

// Base test class to set up common mocks
class BaseServiceTest {
    protected HttpServletResponse response;
    protected Logger logger;

    @BeforeEach
    void setUp() {
        response = mock(HttpServletResponse.class);
        // Use a real logger to avoid Mockito inline-mocking issues on newer JVMs.
        logger = LoggerFactory.getLogger("test");
    }
}
