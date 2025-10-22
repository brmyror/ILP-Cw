package uk.ac.ed.acp.cw2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

// Integration tests for ServiceController endpoints
@SpringBootTest
@AutoConfigureMockMvc
public class ServiceControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Test for the /api/v1/uid endpoint
    @Test
    public void testUIDEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:8080/api/v1/uid"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("s2334630"));
    }

    // Test for the /api/v1/distanceTo endpoint
    @Test
    public void testDistanceToEndpoint() throws Exception {
        String requestBody = """
                {
                  "position1": {
                    "lng": -3.192473,
                    "lat": 55.946233
                  },
                  "position2": {
                    "lng": -3.192473,
                    "lat": 55.946300
                  }
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/api/v1/distanceTo")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json"));
    }

    // Test for the /api/v1/isCloseTo endpoint
    @Test
    public void testIsCloseToEndpoint() throws Exception {
        String requestBody = """
                {
                  "position1": {
                    "lng": -3.192473,
                    "lat": 55.946233
                  },
                  "position2": {
                    "lng": -3.192473,
                    "lat": 55.946300
                  }
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/api/v1/isCloseTo")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json"));
    }

    // Test for the /api/v1/nextPosition endpoint
    @Test
    public void testNextPositionEndpoint() throws Exception {
        String requestBody = """
               {
                 "start": {
                 "lng": -3.192473,
                 "lat": 55.946233
                 },
                 "angle": 45
                }
               """;
        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/api/v1/nextPosition")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json"));
    }

    // Test for the /api/v1/isInRegion endpoint
    @Test
    public void testIsInRegionEndpoint() throws Exception {
        String requestBody = """
                   {
                    "position": {
                        "lng": 1.234,
                        "lat": 1.222
                        },
                        "region": {
                            "name": "central",
                            "vertices": [
                            {
                            "lng": -3.192473,
                            "lat": 55.946233
                            },
                             {
                             "lng": -3.192473,
                             "lat": 55.942617
                             },
                             {
                             "lng": -3.184319,
                             "lat": 55.942617
                             },
                             {
                             "lng": -3.184319,
                             "lat": 55.946233
                             },
                             {
                             "lng": -3.192473,
                             "lat": 55.946233
                            }
                        ]
                    }
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:8080/api/v1/isInRegion")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json"));
    }
}
