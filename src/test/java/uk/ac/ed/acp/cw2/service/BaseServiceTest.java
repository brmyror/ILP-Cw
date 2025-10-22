package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

// Base test class to set up common mocks
class BaseServiceTest {
    protected HttpServletResponse response;
    protected Logger logger;

    @BeforeEach
    void setUp() {
        response = mock(HttpServletResponse.class);
        logger = mock(Logger.class);
    }
}

