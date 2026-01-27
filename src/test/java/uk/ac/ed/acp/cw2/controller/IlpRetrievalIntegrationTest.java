package uk.ac.ed.acp.cw2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.entity.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for FR6: retrieval + mapping of ILP entities.
 *
 * Instead of calling the real ILP network service, we mock RestTemplate at the boundary,
 * ensuring the controller + mappers integrate correctly.
 */
@SpringBootTest
class IlpRetrievalIntegrationTest {

    @Autowired
    private ILPRestController ilpRestController;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void FR6_fetchDrones_mapsDtoToEntity() {
        DroneDto dto = new DroneDto(
                "DroneOne",
                "1",
                new DroneDto.Capabilities(true, false, 5.0, 100, 1.0, 1.0, 1.0)
        );

        when(restTemplate.getForObject(contains("/drones"), eq(DroneDto[].class)))
                .thenReturn(new DroneDto[]{dto});

        List<Drone> drones = ilpRestController.fetchDronesFromIlp();
        assertEquals(1, drones.size());

        Drone d = drones.getFirst();
        assertEquals("1", d.getId());
        assertEquals("DroneOne", d.getName());
        assertTrue(d.getCooling());
        assertFalse(d.getHeating());
        assertEquals(5.0, d.getCapacity());
    }

    @Test
    void FR6_fetchServicePoints_mapsDtoToEntity() {
        DroneServicePointDto dto = new DroneServicePointDto(
                "Appleton Tower",
                1,
                new DroneServicePointDto.Location(-3.192473, 55.946233, null)
        );

        when(restTemplate.getForObject(contains("/service-points"), eq(DroneServicePointDto[].class)))
                .thenReturn(new DroneServicePointDto[]{dto});

        List<DroneServicePoint> sps = ilpRestController.fetchServicePointsFromIlp();
        assertEquals(1, sps.size());
        DroneServicePoint sp = sps.getFirst();
        assertEquals(1, sp.getId());
        assertEquals("Appleton Tower", sp.getName());
        assertNotNull(sp.getLocation());
        assertEquals(-3.192473, sp.getLocation().getLng(), 1e-12);
        assertEquals(55.946233, sp.getLocation().getLat(), 1e-12);
    }

    @Test
    void FR6_fetchRestrictedAreas_mapsDtoToEntity() {
        RestrictedAreaDto.Vertices v1 = new RestrictedAreaDto.Vertices(-3.0, 55.0);
        RestrictedAreaDto.Vertices v2 = new RestrictedAreaDto.Vertices(-3.0, 55.1);
        RestrictedAreaDto.Vertices v3 = new RestrictedAreaDto.Vertices(-2.9, 55.1);
        RestrictedAreaDto.Vertices v4 = new RestrictedAreaDto.Vertices(-3.0, 55.0);
        RestrictedAreaDto dto = new RestrictedAreaDto(
                "TestArea",
                1,
                new RestrictedAreaDto.Limits(0, -1),
                new RestrictedAreaDto.Vertices[]{v1, v2, v3, v4}
        );

        when(restTemplate.getForObject(contains("/restricted-areas"), eq(RestrictedAreaDto[].class)))
                .thenReturn(new RestrictedAreaDto[]{dto});

        List<RestrictedArea> areas = ilpRestController.fetchRestrictedAreasFromIlp();
        assertEquals(1, areas.size());
        RestrictedArea a = areas.getFirst();
        assertEquals("TestArea", a.getName());
        assertEquals(1, a.getId());
        assertNotNull(a.getLimits());
        assertNotNull(a.getVertices());
        assertEquals(4, a.getVertices().length);
    }

    @Test
    void FR6_fetchDronesForServicePoints_mapsDtoToEntity() {
        DroneForServicePointDto.DroneAvailability.Availability av =
                new DroneForServicePointDto.DroneAvailability.Availability(DayOfWeek.FRIDAY, LocalTime.of(0, 0), LocalTime.of(23, 59));
        DroneForServicePointDto.DroneAvailability da =
                new DroneForServicePointDto.DroneAvailability("1", new DroneForServicePointDto.DroneAvailability.Availability[]{av});
        DroneForServicePointDto dto = new DroneForServicePointDto(1, new DroneForServicePointDto.DroneAvailability[]{da});

        when(restTemplate.getForObject(contains("/drones-for-service-points"), eq(DroneForServicePointDto[].class)))
                .thenReturn(new DroneForServicePointDto[]{dto});

        List<DroneForServicePoint> mappings = ilpRestController.fetchDronesForServicePointsFromIlp();
        assertEquals(1, mappings.size());
        DroneForServicePoint m = mappings.getFirst();
        assertEquals(1, m.getServicePointId());
        assertNotNull(m.getDrones());
        assertEquals(1, m.getDrones().length);
        assertEquals("1", m.getDrones()[0].getId());
    }
}
