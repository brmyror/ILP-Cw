package uk.ac.ed.acp.cw2.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.dto.CalculatedDeliveryPathRequest;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.dto.MedDispatchRecRequest;
import uk.ac.ed.acp.cw2.dto.QueryRequest;
import uk.ac.ed.acp.cw2.entity.Deliveries;
import uk.ac.ed.acp.cw2.entity.Drone;
import uk.ac.ed.acp.cw2.entity.DroneForServicePoint;
import uk.ac.ed.acp.cw2.entity.DronePaths;
import uk.ac.ed.acp.cw2.entity.DroneServicePoint;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests primarily aimed at improving DroneService line/branch coverage.
 * These are deterministic unit tests that exercise multiple branches.
 */
class DroneServiceCoverageTest {

    private final DroneService service = new DroneService();

    @Test
    void dronesWithCooling_filtersByState() {
        List<Drone> drones = List.of(
                Drone.builder().id("1").name("d1").cooling(true).heating(false).capacity(1.0).maxMoves(10).costPerMove(1.0).costInitial(1.0).costFinal(1.0).build(),
                Drone.builder().id("2").name("d2").cooling(false).heating(false).capacity(1.0).maxMoves(10).costPerMove(1.0).costInitial(1.0).costFinal(1.0).build()
        );

        assertArrayEquals(new String[]{"1"}, service.dronesWithCooling(true, drones));
        assertArrayEquals(new String[]{"2"}, service.dronesWithCooling(false, drones));
    }

    @Test
    void queryAvailableDrones_coversDateAndTimeBranches_costAndCapacity_coolingAndHeating() {
        Drone d1 = Drone.builder().id("1").name("d1")
                .cooling(true).heating(true)
                .capacity(5.0)
                .maxMoves(10)
                .costPerMove(1.0).costInitial(1.0).costFinal(1.0)
                .build();
        Drone d2 = Drone.builder().id("2").name("d2")
                .cooling(false).heating(true)
                .capacity(5.0)
                .maxMoves(0) // triggers maxMoves < 1 branch when maxCost is set
                .costPerMove(1.0).costInitial(1.0).costFinal(1.0)
                .build();

        // Availability windows: d1 only on Monday 10:00-12:00; d2 has a window covering any time on Monday.
        DroneForServicePoint.Availability a1 = DroneForServicePoint.Availability.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .from(LocalTime.of(10, 0))
                .until(LocalTime.of(12, 0))
                .build();
        DroneForServicePoint.Availability a2 = DroneForServicePoint.Availability.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .from(LocalTime.of(0, 0))
                .until(LocalTime.of(23, 59))
                .build();

        DroneForServicePoint.DroneAvailability da1 = DroneForServicePoint.DroneAvailability.builder()
                .id("1")
                .availability(new DroneForServicePoint.Availability[]{a1})
                .build();
        DroneForServicePoint.DroneAvailability da2 = DroneForServicePoint.DroneAvailability.builder()
                .id("2")
                .availability(new DroneForServicePoint.Availability[]{a2})
                .build();

        DroneForServicePoint dfsp = DroneForServicePoint.builder()
                .servicePointId(1)
                .drones(new DroneForServicePoint.DroneAvailability[]{da1, da2})
                .build();

        // Request 1: date+time set (hits date!=null branch); requires cooling/heating; maxCost set
        MedDispatchRecRequest.Requirements r1 = new MedDispatchRecRequest.Requirements(2.0, true, true, 5.0);
        MedDispatchRecRequest req1 = new MedDispatchRecRequest(1, LocalDate.of(2025, 1, 6), LocalTime.of(11, 0), r1,
                LngLat.builder().lng(-3.0).lat(55.0).build());

        // Request 2: date null but time set (hits date==null branch); requires heating only; no maxCost
        MedDispatchRecRequest.Requirements r2 = new MedDispatchRecRequest.Requirements(2.0, false, true, null);
        MedDispatchRecRequest req2 = new MedDispatchRecRequest(2, null, LocalTime.of(11, 0), r2,
                LngLat.builder().lng(-3.0).lat(55.0).build());

        // Request 3: date and time null (hits no constraint early return in filter)
        MedDispatchRecRequest.Requirements r3 = new MedDispatchRecRequest.Requirements(1.0, false, false, null);
        MedDispatchRecRequest req3 = new MedDispatchRecRequest(3, null, null, r3,
                LngLat.builder().lng(-3.0).lat(55.0).build());

        String[] ids = service.queryAvailableDrones(List.of(req1, req2, req3), List.of(d1, d2), List.of(dfsp));

        // d2 is filtered out by maxMoves<1 when req1 has maxCost; d1 remains and has enough capacity.
        assertArrayEquals(new String[]{"1"}, ids);
        // and capacity was decremented by reqs that passed capacity filter
        assertEquals(0.0, d1.getCapacity(), 1e-9);
    }

    @Test
    void query_coversAllOperators_andRemovalLogic() {
        List<Drone> drones = List.of(
                Drone.builder().id("1").name("d1").cooling(true).heating(false).capacity(10.0).maxMoves(10).costPerMove(1.0).costInitial(1.0).costFinal(1.0).build(),
                Drone.builder().id("2").name("d2").cooling(false).heating(false).capacity(5.0).maxMoves(10).costPerMove(1.0).costInitial(1.0).costFinal(1.0).build()
        );

        // Each query removes matching drones from consideration, so we expect at most 1 id per query step.
        List<QueryRequest> qs = List.of(
                new QueryRequest("cooling", "=", "true"),
                new QueryRequest("capacity", ">", "6"),
                new QueryRequest("capacity", "<", "6"),
                new QueryRequest("cooling", "!=", "true")
        );

        String[] ids = service.query(qs, drones);
        // First query matches drone 1 and removes it.
        // Second query now sees drone 2 (capacity 5) so it doesn't match >6.
        // Third query matches drone 2 (<6) and removes it.
        // Fourth query sees no remaining drones.
        assertArrayEquals(new String[]{"1", "2"}, ids);
    }

    @Test
    void buildGeoJsonObject_handlesNullAndSingleDronePathsBranches() {
        // null input branch
        assertEquals("FeatureCollection", service.buildGeoJsonObject(null).get("type").asText());
        assertTrue(service.buildGeoJsonObject(null).get("features").isArray());

        // single-drone branch where totalMoves/totalCost are overridden from calc
        LngLat p1 = LngLat.builder().lng(0.0).lat(0.0).build();
        LngLat p2 = LngLat.builder().lng(0.00015).lat(0.0).build();
        Deliveries del1 = Deliveries.builder().deliveryId(10).flightPath(new LngLat[]{p1, p2}).build();
        DronePaths dp = DronePaths.builder().droneId(1).deliveries(new Deliveries[]{del1}).build();

        CalculatedDeliveryPathRequest calc = CalculatedDeliveryPathRequest.builder()
                .totalMoves(1)
                .totalCost(3.0)
                .dronePaths(new DronePaths[]{dp})
                .build();

        var geo = service.buildGeoJsonObject(calc);
        assertEquals("FeatureCollection", geo.get("type").asText());
        assertTrue(geo.get("features").isArray());
        assertEquals("Feature", geo.get("features").get(0).get("type").asText());
        assertEquals("LineString", geo.get("features").get(0).get("geometry").get("type").asText());
        assertEquals("1", geo.get("features").get(0).get("properties").get("droneId").asText());
        assertEquals(1, geo.get("features").get(0).get("properties").get("totalMoves").asInt());
        assertEquals(3.0, geo.get("features").get(0).get("properties").get("totalCost").asDouble(), 1e-9);
    }

    @Test
    void calcDeliveryPath_noDrones_returnsDiagnosticsNoAvailableDrones() {
        var calc = service.calcDeliveryPath(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new String[0],
                List.of()
        );
        assertNotNull(calc);
        assertEquals(0, calc.getTotalMoves());
        assertEquals(0.0, calc.getTotalCost(), 1e-9);
        assertNotNull(calc.getDiagnostics());
        assertEquals("NO_AVAILABLE_DRONES", calc.getDiagnostics().getReasonCode());
    }

    @Test
    void droneDetails_returnsDrone_whenFound_andThrows404_whenNotFound() {
        List<Drone> drones = List.of(
                Drone.builder().id("1").name("d1").cooling(true).heating(false).capacity(1.0).maxMoves(10)
                        .costPerMove(1.0).costInitial(1.0).costFinal(1.0).build()
        );

        Drone found = service.droneDetails("1", drones);
        assertNotNull(found);
        assertEquals("1", found.getId());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.droneDetails("missing", drones));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void queryAsPath_filtersUsingDynamicPropertyPath() {
        List<Drone> drones = List.of(
                Drone.builder().id("1").name("d1").cooling(true).heating(false).capacity(10.0).maxMoves(10)
                        .costPerMove(1.0).costInitial(1.0).costFinal(1.0).build(),
                Drone.builder().id("2").name("d2").cooling(false).heating(false).capacity(10.0).maxMoves(10)
                        .costPerMove(1.0).costInitial(1.0).costFinal(1.0).build()
        );

        // boolean attribute via getCooling()
        assertArrayEquals(new String[]{"1"}, service.queryAsPath("cooling", "true", drones));

        // numeric attribute via getCapacity()
        assertArrayEquals(new String[]{"1", "2"}, service.queryAsPath("capacity", "10.0", drones));

        // unknown attribute -> readProperty returns null -> attributeEquals false
        assertArrayEquals(new String[0], service.queryAsPath("doesNotExist", "x", drones));
    }

    @Test
    void calcDeliveryPath_multiDelivery_hitsLastDeliveryMultiBranch_andGeneratesReturnLeg() {
        Drone drone = Drone.builder().id("1").name("d1")
                .cooling(true).heating(false).capacity(10.0).maxMoves(2000)
                .costPerMove(1.0).costInitial(1.0).costFinal(1.0)
                .build();

        DroneServicePoint sp = DroneServicePoint.builder()
                .id(1)
                .name("SP")
                .location(LngLat.builder().lng(-3.192473).lat(55.946233).build())
                .build();

        // two deliveries ensures we hit:
        //  - i==0 branch
        //  - i==last AND deliveries.size()>1 branch (the one you pointed out)
        MedDispatchRecRequest.Requirements reqs = new MedDispatchRecRequest.Requirements(1.0, false, false, null);
        MedDispatchRecRequest d1 = new MedDispatchRecRequest(
                101,
                null,
                null,
                reqs,
                LngLat.builder().lng(-3.191).lat(55.946).build());
        MedDispatchRecRequest d2 = new MedDispatchRecRequest(
                102,
                null,
                null,
                reqs,
                LngLat.builder().lng(-3.1905).lat(55.9455).build());

        // dronesForServicePoints provides a mapped originSp so we avoid the fallback-origin branch
        DroneForServicePoint.Availability av = DroneForServicePoint.Availability.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .from(LocalTime.of(0, 0))
                .until(LocalTime.of(23, 59))
                .build();
        DroneForServicePoint.DroneAvailability da = DroneForServicePoint.DroneAvailability.builder()
                .id("1")
                .availability(new DroneForServicePoint.Availability[]{av})
                .build();
        DroneForServicePoint dfsp = DroneForServicePoint.builder()
                .servicePointId(1)
                .drones(new DroneForServicePoint.DroneAvailability[]{da})
                .build();

        CalculatedDeliveryPathRequest calc = service.calcDeliveryPath(
                List.of(d1, d2),
                List.of(drone),
                List.of(sp),
                List.of(),
                new String[]{"1"},
                List.of(dfsp)
        );

        assertNotNull(calc);
        assertNotNull(calc.getDronePaths());
        assertEquals(1, calc.getDronePaths().length);
        assertNotNull(calc.getDronePaths()[0].getDeliveries());

        // For 2 deliveries we expect:
        // - delivery 101 segment (i==0)
        // - delivery 102 outbound (multi last branch)
        // - return leg with null id (multi last branch)
        assertTrue(calc.getDronePaths()[0].getDeliveries().length >= 3);

        boolean sawSecondDelivery = false;
        boolean sawReturnLeg = false;
        for (var del : calc.getDronePaths()[0].getDeliveries()) {
            if (Integer.valueOf(102).equals(del.getDeliveryId())) sawSecondDelivery = true;
            if (del.getDeliveryId() == null) sawReturnLeg = true;
        }
        assertTrue(sawSecondDelivery);
        assertTrue(sawReturnLeg);
    }
}
