package uk.ac.ed.acp.cw2.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.entity.*;
import uk.ac.ed.acp.cw2.testutil.TestDataLoader;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * System-level tests for FR9/FR10/MR3/MR4 using MockMvc end-to-end HTTP calls.
 *
 * We mock ILPRestController so the tests are deterministic and don't depend on the external ILP service.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PlannerEndpointsSystemTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ILPRestController ilpRestController;

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private List<MedDispatchRecRequest> dispatchScenario1;
    private List<RestrictedArea> restrictedAreas;

    @PostConstruct
    void initFixtures() {
        // Use a realistic scenario payload from the visualiser project
        this.dispatchScenario1 = TestDataLoader.loadDispatchScenarioUseCase("sample-1");

        // Restricted areas from the visualiser (for FR11/MR5 related tests elsewhere)
        RestrictedAreaDto[] raDtos = TestDataLoader.loadRestrictedAreas();
        this.restrictedAreas = uk.ac.ed.acp.cw2.mapper.RestrictedAreaMapper.fromDtoList(raDtos);

        // Stub ILP responses for system tests.
        when(ilpRestController.fetchRestrictedAreasFromIlp()).thenReturn(restrictedAreas);
        when(ilpRestController.fetchDronesFromIlp()).thenReturn(List.of(stubDrone("1")));
        when(ilpRestController.fetchServicePointsFromIlp()).thenReturn(List.of(stubServicePoint(1, -3.192473, 55.946233)));
        when(ilpRestController.fetchDronesForServicePointsFromIlp()).thenReturn(List.of(stubDroneForServicePoint(1, "1")));
    }

    @Test
    void FR9_calcDeliveryPath_returnsPlanWithTotals_and_MR4_deterministicForSameInput() throws Exception {
        String jsonBody = MAPPER.writeValueAsString(dispatchScenario1);

        // First request
        MvcResult r1 = mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                // FR9: totals present
                .andExpect(jsonPath("$.totalMoves").isNumber())
                .andExpect(jsonPath("$.totalCost").isNumber())
                .andExpect(jsonPath("$.dronePaths").isArray())
                .andReturn();

        // Second request (same input)
        MvcResult r2 = mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andReturn();

        // MR4: deterministic outputs for identical inputs.
        // We compare key fields because diagnostics include a requestId which is expected to differ.
        JsonNode j1 = MAPPER.readTree(r1.getResponse().getContentAsString());
        JsonNode j2 = MAPPER.readTree(r2.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertEquals(j1.path("totalMoves").asInt(), j2.path("totalMoves").asInt());
        org.junit.jupiter.api.Assertions.assertEquals(j1.path("totalCost").asDouble(), j2.path("totalCost").asDouble(), 1e-9);
        org.junit.jupiter.api.Assertions.assertEquals(j1.path("dronePaths").toString(), j2.path("dronePaths").toString());
    }

    @Test
    void FR10_calcDeliveryPathAsGeoJson_returnsValidGeoJsonFeatureCollection() throws Exception {
        String jsonBody = MAPPER.writeValueAsString(dispatchScenario1);

        mockMvc.perform(post("/api/v1/calcDeliveryPathAsGeoJson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                // GeoJSON envelope
                .andExpect(jsonPath("$.type").value("FeatureCollection"))
                .andExpect(jsonPath("$.features").isArray())
                // At least one feature with LineString coordinates
                .andExpect(jsonPath("$.features[0].type").value("Feature"))
                .andExpect(jsonPath("$.features[0].geometry.type").value("LineString"))
                .andExpect(jsonPath("$.features[0].geometry.coordinates").isArray())
                // properties needed by visualiser integration
                .andExpect(jsonPath("$.features[0].properties.droneId").exists())
                .andExpect(jsonPath("$.features[0].properties.totalMoves").isNumber());
    }

    @Test
    void MR3_typicalResponseTime_under2000ms_for_calcDeliveryPath() throws Exception {
        String jsonBody = MAPPER.writeValueAsString(dispatchScenario1);

        long start = System.nanoTime();
        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        // MR3: This is a timing test. It's inherently environment-dependent.
        // Use a generous threshold for CI/dev machines; document the target in the portfolio.
        org.junit.jupiter.api.Assertions.assertTrue(durationMs <= 2000,
                "Expected <=2000ms but was " + durationMs + "ms");
    }

    private static Drone stubDrone(String id) {
        return Drone.builder()
                .id(id)
                .name("Test drone")
                .cooling(true)
                .heating(false)
                .capacity(10.0)
                .maxMoves(2000)
                .costPerMove(1.0)
                .costInitial(1.0)
                .costFinal(1.0)
                .build();
    }

    private static DroneServicePoint stubServicePoint(int id, double lng, double lat) {
        return DroneServicePoint.builder()
                .id(id)
                .name("ServicePoint-" + id)
                .location(LngLat.builder().lng(lng).lat(lat).build())
                .build();
    }

    private static DroneForServicePoint stubDroneForServicePoint(int servicePointId, String droneId) {
        DroneForServicePoint.Availability av = DroneForServicePoint.Availability.builder()
                .dayOfWeek(java.time.DayOfWeek.FRIDAY)
                .from(java.time.LocalTime.of(0, 0))
                .until(java.time.LocalTime.of(23, 59))
                .build();

        DroneForServicePoint.DroneAvailability da = DroneForServicePoint.DroneAvailability.builder()
                .id(droneId)
                .availability(new DroneForServicePoint.Availability[]{av})
                .build();

        return DroneForServicePoint.builder()
                .servicePointId(servicePointId)
                .drones(new DroneForServicePoint.DroneAvailability[]{da})
                .build();
    }
}
