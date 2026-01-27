package uk.ac.ed.acp.cw2.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.data.Distance;
import uk.ac.ed.acp.cw2.data.DynamicQueries;
import uk.ac.ed.acp.cw2.data.FlightPathAlgorithm;
import uk.ac.ed.acp.cw2.dto.CalculatedDeliveryPathRequest;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.dto.MedDispatchRecRequest;
import uk.ac.ed.acp.cw2.dto.PlanningDiagnostics;
import uk.ac.ed.acp.cw2.dto.QueryRequest;
import uk.ac.ed.acp.cw2.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

@Service
public class DroneService {

    private static final Logger logger = LoggerFactory.getLogger(DroneService.class);

    // Executor for offloading pathfinding to avoid blocking request threads
    private static final ExecutorService PATH_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("path-exec-") ;
        return t;
    });

    public String[] dronesWithCooling(Boolean state, List<Drone> drones) {
        // Filter by cooling capability and return matching IDs
        return drones.stream()
                .filter(d -> Objects.equals(d.getCooling(), state))
                .map(Drone::getId)
                .toArray(String[]::new);
    }

    public Drone droneDetails(String id, List<Drone> drones) {
        // Find a drone by id, then map to existing Drone shape
        return drones.stream()
                .filter(d -> id.equals(d.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drone not found"));
    }

    //    "maxCost": 13.5
    public String[] queryAvailableDrones(List<MedDispatchRecRequest> req, List<Drone> drones,
                                         List<DroneForServicePoint> dronesForServicePoints) {
        List<Drone> availableDrones = new ArrayList<>(drones);

        if (req == null) {
            return availableDrones.stream().map(Drone::getId).toArray(String[]::new);
        }

        // Build a map of drone id -> list of availability windows for fast lookup
        Map<String, List<DroneForServicePoint.Availability>> availabilityMap = new HashMap<>();
        for (DroneForServicePoint sp : dronesForServicePoints) {
            for (DroneForServicePoint.DroneAvailability da : sp.getDrones()) {
                List<DroneForServicePoint.Availability> list = availabilityMap.computeIfAbsent(da.getId(), k -> new ArrayList<>());
                list.addAll(Arrays.asList(da.getAvailability()));
            }
        }

        for (MedDispatchRecRequest r : req) {
            Double requiredCapacity = r.getRequirements().getCapacity();
            boolean requiresCooling = r.getRequirements().isCooling();
            boolean requiresHeating = r.getRequirements().isHeating();
            LocalDate date = r.getDate();
            LocalTime time = r.getTime();
            // Use a conservative lower-bound estimate for cost when maxCost is provided:
            // minCostEstimate = costInitial + costFinal + costPerMove * distance(servicePoint, delivery)/step

            availableDrones = availableDrones.stream()
                    .filter(d -> {
                        if (date == null && time == null) return true; // no constraint

                        List<DroneForServicePoint.Availability> avails = availabilityMap.getOrDefault(d.getId(), Collections.emptyList());

                        // If date provided, check day-of-week match; if time provided, also check the time window
                        if (date != null) {
                            DayOfWeek dow = date.getDayOfWeek();
                            return avails.stream().anyMatch(a -> a.getDayOfWeek() == dow &&
                                    (time == null || (!a.getFrom().isAfter(time) && !a.getUntil().isBefore(time))));
                        } else { // date == null but time != null: accept any availability window that contains the time
                            return avails.stream().anyMatch(a -> (!a.getFrom().isAfter(time) && !a.getUntil().isBefore(time)));
                        }
                    })
                    // check cost BEFORE consuming capacity so we don't mutate drones that will be filtered out
                    .filter(d -> {
                        Double maxCost = r.getRequirements().getMaxCost();
                        if (maxCost == null) return true;

                        double costPerMove = d.getCostPerMove();
                        double costInitial = d.getCostInitial();
                        double costFinal = d.getCostFinal();
                        int maxMoves = d.getMaxMoves();

                        // if drone cannot make any move, it can't serve a delivery that requires movement
                        if (maxMoves < 1) return false;

                        double minEstimate = costInitial + costFinal + costPerMove;
                        return Double.compare(minEstimate, maxCost) <= 0;
                    })
                    .filter(d -> {
                        Double cap = d.getCapacity();

                        if (Double.compare(cap, requiredCapacity) >= 0) {
                            double newCap = cap - requiredCapacity;
                            d.setCapacity(newCap);
                            return true;
                        }
                        return false;
                    })
                    .filter(d -> !requiresCooling || Boolean.TRUE.equals(d.getCooling()))
                    .filter(d -> !requiresHeating || Boolean.TRUE.equals(d.getHeating()))
                    .toList();
        }


        return availableDrones.stream()
                .map(Drone::getId)
                .toArray(String[]::new);
    }

    public String[] queryAsPath(String attributeName, String value, List<Drone> drones) {
        List<String> matched = new ArrayList<>();
        for (Drone d : drones) {
            Object attrVal = DynamicQueries.readProperty(d, attributeName);
            if (DynamicQueries.attributeEquals(attrVal, value)) {
                matched.add(d.getId());
            }
        }
        return matched.toArray(new String[0]);
    }

    public String[] query(List<QueryRequest> query, List<Drone> drones) {
        List<Drone> availableDrones = new ArrayList<>(drones);
        List<String> droneIDs = new ArrayList<>();
        for (QueryRequest q : query) {
            Iterator<Drone> iterator = availableDrones.iterator();
            while (iterator.hasNext()) {
                Drone d = iterator.next();
                Object attrVal = DynamicQueries.readProperty(d, q.getAttribute());
                boolean match = false;
                if (Objects.equals(q.getOperator(), "=")) {
                    match = DynamicQueries.attributeEquals(attrVal, q.getValue());
                }
                 else if (Objects.equals(q.getOperator(), "!=")) {
                    match = !DynamicQueries.attributeEquals(attrVal, q.getValue());
                }
                 else if (Objects.equals(q.getOperator(), ">")) {
                    match = DynamicQueries.attributeGreaterThan(attrVal, q.getValue());
                }
                 else if (Objects.equals(q.getOperator(), "<")) {
                    match = DynamicQueries.attributeLessThan(attrVal, q.getValue());
                }
                 if (match) {
                    droneIDs.add(d.getId());
                    iterator.remove();
                 }
            }
        }
        return droneIDs.toArray(new String[0]);
    }

    public CalculatedDeliveryPathRequest calcDeliveryPath(List<MedDispatchRecRequest> req, List<Drone> drones,
                                                          List<DroneServicePoint> servicePoints,
                                                          List<RestrictedArea> restrictedAreas, String[] droneIDs,
                                                          List<DroneForServicePoint> dronesForServicePoints) {

        // lightweight instrumentation: request correlation + end-to-end timing
        final String requestId = UUID.randomUUID().toString();
        final long startNs = System.nanoTime();

        // counters for diagnosing path planning behaviour
        final int[] legsPlanned = {0};
        final int[] legsFailed = {0};
        final int[] aStarInvocations = {0};
        final int[] straightLineFallbacks = {0};
        String reasonCode = null;

        logger.info("calcDeliveryPath called: requestId={}, reqSize={}, dronesSize={}, servicePointsSize={}, dronesForServicePointsSize={}, restrictedAreasSize={}, droneIDs={}",
                requestId,
                req == null ? 0 : req.size(),
                drones == null ? 0 : drones.size(),
                servicePoints == null ? 0 : servicePoints.size(),
                dronesForServicePoints == null ? 0 : dronesForServicePoints.size(),
                restrictedAreas == null ? 0 : restrictedAreas.size(),
                droneIDs == null ? "[]" : Arrays.toString(droneIDs));

        // In case no available drones, return the empty response
        if (droneIDs == null || droneIDs.length == 0) {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            PlanningDiagnostics diag = PlanningDiagnostics.builder()
                    .requestId(requestId)
                    .durationMs(durationMs)
                    .reasonCode("NO_AVAILABLE_DRONES")
                    .legsPlanned(0)
                    .legsFailed(0)
                    .aStarInvocations(0)
                    .straightLineFallbacks(0)
                    .build();
            return CalculatedDeliveryPathRequest.builder().totalCost(0.0).totalMoves(0).dronePaths(new DronePaths[0]).diagnostics(diag).build();
        }

        // Map drone id -> Drone entity for cost lookups
        Map<String, Drone> droneById = new HashMap<>();
        for (Drone d : drones) droneById.put(d.getId(), d);

        // Evenly distribute requests to drone IDs
        Map<String, List<MedDispatchRecRequest>> assignments = new LinkedHashMap<>();
        for (String id : droneIDs) assignments.put(id, new ArrayList<>());

        int idx = 0;
        if (req == null || req.isEmpty()) {return CalculatedDeliveryPathRequest.builder().build();}
        for (MedDispatchRecRequest m : req) {
            String assigned = droneIDs[idx % droneIDs.length];
            assignments.get(assigned).add(m);
            idx++;
        }

        // log assignments for diagnostics
        logger.info("Assignments built: {}", assignments.entrySet().stream().map(e -> e.getKey() + "->" + e.getValue().size()).toList());

        Map<String, DroneServicePoint> droneHomeById = new HashMap<>();
        if (dronesForServicePoints != null && servicePoints != null) {
            for (DroneForServicePoint dfsp : dronesForServicePoints) {
                // find the matching service point (dfsp.servicePointId -> DroneServicePoint.getId())
                Integer spId = dfsp.getServicePointId();
                DroneServicePoint matched = null;
                if (spId != null) {
                    for (DroneServicePoint sp : servicePoints) {
                        if (sp != null && Objects.equals(sp.getId(), spId)) {
                            matched = sp;
                            break;
                        }
                    }
                }

                // map every drone availability entry's id to the matched service point
                if (dfsp.getDrones() != null) {
                    for (DroneForServicePoint.DroneAvailability da : dfsp.getDrones()) {
                        if (da != null && da.getId() != null && matched != null) {
                            droneHomeById.put(da.getId(), matched);
                        }
                    }
                }
            }
        }

        // log droneHomeById for diagnostics
        logger.info("droneHomeById mapping: {}", droneHomeById.entrySet().stream().map(e -> e.getKey() + "->" + (e.getValue()==null?"null":e.getValue().getId())).toList());

        // log servicePoints for diagnostics
        logger.info("servicePoints: {}", servicePoints.stream().map(sp -> {
            if (sp == null) return "null";
            if (sp.getLocation() == null) return sp.getId() + "->location=null";
            LngLat l = sp.getLocation();
            return sp.getId() + "->(" + l.getLng() + "," + l.getLat() + ")";
        }).toList());

        // log dronesForServicePoints for diagnostics
        logger.info("dronesForServicePoints count: {}", dronesForServicePoints.size());

        int totalMoves = 0;
        double totalCost = 0.0;
        List<DronePaths> dronePaths = new ArrayList<>();

        // For each drone, build sequential flightPaths
        for (String droneId : assignments.keySet()) {
            List<MedDispatchRecRequest> deliveries = assignments.get(droneId);
            if (deliveries == null || deliveries.isEmpty()) {
                logger.info("No deliveries assigned to drone {} (requestId={}) (skipping)", droneId, requestId);
                continue;

            }

            // Determine service point origin for this drone: prefer mapped home, otherwise nearest to first delivery
            DroneServicePoint originSp = null;
            if (droneHomeById.containsKey(droneId)) {
                originSp = droneHomeById.get(droneId);
            }

            LngLat firstDeliveryPos = null;
            if (!deliveries.isEmpty() && deliveries.getFirst() != null) {
                firstDeliveryPos = deliveries.getFirst().getDelivery();

            }

            // If no origin service point was found, but we have a valid first delivery position, use it as fallback origin.
            if (originSp == null && firstDeliveryPos != null) {
                originSp = DroneServicePoint.builder()
                        .id(-1)
                        .name("fallback-origin")
                        .location(firstDeliveryPos)
                        .build();
                logger.info("Drone {}: no service point origin found; using firstDeliveryPos as fallback origin", droneId);
            }

            // log chosen origin for this drone
            logger.info("Drone {}: chosen originSp id={} (location={})", droneId,
                    originSp == null ? "null" : String.valueOf(originSp.getId()),
                    originSp == null || originSp.getLocation() == null ? "null" : (originSp.getLocation().getLng() + "," + originSp.getLocation().getLat()));

            List<Deliveries> droneDeliveries = new ArrayList<>();

            // build segments of the total flight path
            for (int i = 0; i < deliveries.size(); i++) {
                MedDispatchRecRequest curr = deliveries.get(i);
                LngLat start;
                LngLat end;
                List<LngLat> seg;

                if (i == deliveries.size() - 1) {
                    // last delivery: start at the previous delivery (or origin if only one), go to last delivery then return to origin
                    if (deliveries.size() == 1) {
                        // single delivery: split outbound and return into two Deliveries
                        start = originSp.getLocation();
                        end = curr.getDelivery();
                        legsPlanned[0]++;
                        List<LngLat> firstLeg = new ArrayList<>(safeFindPath(start, end, restrictedAreas, requestId, aStarInvocations, straightLineFallbacks, legsFailed));
                        logger.info("Drone {}: firstLeg size from findPath(start={}, end={}) = {} (requestId={})", droneId, start, end, firstLeg.size(), requestId);

                        // hover at end -> duplicate last point on outbound leg
                        if (!firstLeg.isEmpty()) {
                            LngLat last = firstLeg.getLast();
                            firstLeg.add(LngLat.builder().lng(last.getLng()).lat(last.getLat()).build());
                        }

                        legsPlanned[0]++;
                        List<LngLat> returnLeg = new ArrayList<>(safeFindPath(end, originSp.getLocation(), restrictedAreas, requestId, aStarInvocations, straightLineFallbacks, legsFailed));
                        logger.info("Drone {}: returnLeg size from findPath(end={}, origin={}) = {} (requestId={})", droneId, end, originSp.getLocation(), returnLeg.size(), requestId);

                        // compute moves and cost for outbound leg
                        int movesOut = Math.max(0, firstLeg.size() - 1);
                        totalMoves += movesOut;
                        Drone assignedDroneOut = droneById.get(droneId);
                        double costOut = 0.0;
                        if (assignedDroneOut != null) {
                            double costPerMove = assignedDroneOut.getCostPerMove();
                            double costInitial = assignedDroneOut.getCostInitial();
                            double costFinal = assignedDroneOut.getCostFinal();
                            costOut = costInitial + costFinal + costPerMove * movesOut;
                        }
                        totalCost += costOut;

                        Deliveries delOut = Deliveries.builder()
                                .deliveryId(curr.getId())
                                .flightPath(firstLeg.toArray(new LngLat[0]))
                                .build();
                        droneDeliveries.add(delOut);

                        // compute moves and cost for return leg (as a separate delivery with null id)
                        int movesRet = Math.max(0, returnLeg.size() - 1);
                        totalMoves += movesRet;
                        double costRet = 0.0;
                        if (assignedDroneOut != null) {
                            double costPerMove = assignedDroneOut.getCostPerMove();
                            double costInitial = assignedDroneOut.getCostInitial();
                            double costFinal = assignedDroneOut.getCostFinal();
                            costRet = costInitial + costFinal + costPerMove * movesRet;
                        }
                        totalCost += costRet;

                        Deliveries delRet = Deliveries.builder()
                                .deliveryId(null)
                                .flightPath(returnLeg.toArray(new LngLat[0]))
                                .build();
                        droneDeliveries.add(delRet);

                        // done with this (and only) delivery
                    } else {
                        // multiple deliveries: last delivery is outbound then return concatenated into one segment
                        start = deliveries.get(i - 1).getDelivery();
                        end = curr.getDelivery();

                        legsPlanned[0]++;
                        List<LngLat> firstLeg = new ArrayList<>(safeFindPath(start, end, restrictedAreas, requestId, aStarInvocations, straightLineFallbacks, legsFailed));
                        logger.info("Drone {}: firstLeg size (multi) from findPath(start={}, end={}) = {} (requestId={})", droneId, start, end, firstLeg.size(), requestId);
                        // hover at end -> duplicate last point on outbound leg
                        if (!firstLeg.isEmpty()) {
                            LngLat last = firstLeg.getLast();
                            firstLeg.add(LngLat.builder().lng(last.getLng()).lat(last.getLat()).build());
                        }

                        legsPlanned[0]++;
                        List<LngLat> returnLeg = new ArrayList<>(safeFindPath(end, originSp.getLocation(), restrictedAreas, requestId, aStarInvocations, straightLineFallbacks, legsFailed));
                        logger.info("Drone {}: returnLeg size (multi) from findPath(end={}, origin={}) = {} (requestId={})", droneId, end, originSp.getLocation(), returnLeg.size(), requestId);

                        // compute moves and cost for this delivery (concatenated round-trip)
                        int moves = Math.max(0, firstLeg.size() - 1);
                        totalMoves += moves;

                        Drone assignedDrone = droneById.get(droneId);
                        double cost = 0.0;
                        if (assignedDrone != null) {
                            double costPerMove = assignedDrone.getCostPerMove();
                            double costInitial = assignedDrone.getCostInitial();
                            double costFinal = assignedDrone.getCostFinal();
                            cost = costInitial + costFinal + costPerMove * moves;
                        }
                        totalCost += cost;

                        Deliveries del = Deliveries.builder()
                                .deliveryId(curr.getId())
                                .flightPath(firstLeg.toArray(new LngLat[0]))
                                .build();
                        droneDeliveries.add(del);

                        // compute moves and cost for return leg (as a separate delivery with null id)
                        int movesRet = Math.max(0, returnLeg.size() - 1);
                        totalMoves += movesRet;
                        double costRet = 0.0;
                        if (assignedDrone != null) {
                            double costPerMove = assignedDrone.getCostPerMove();
                            double costInitial = assignedDrone.getCostInitial();
                            double costFinal = assignedDrone.getCostFinal();
                            costRet = costInitial + costFinal + costPerMove * movesRet;
                        }
                        totalCost += costRet;

                        Deliveries delRet = Deliveries.builder()
                                .deliveryId(null)
                                .flightPath(returnLeg.toArray(new LngLat[0]))
                                .build();
                        droneDeliveries.add(delRet);
                    }
                    continue;
                } else if (i == 0) {
                    // start at service point, go to the first delivery
                    assert originSp != null;
                    start = originSp.getLocation();
                    end = curr.getDelivery();
                    legsPlanned[0]++;
                    seg = new ArrayList<>(safeFindPath(start, end, restrictedAreas, requestId, aStarInvocations, straightLineFallbacks, legsFailed));
                    logger.info("Drone {}: seg size (i==0) from findPath(start={}, end={}) = {} (requestId={})", droneId, start, end, seg.size(), requestId);

                    // hover at end -> duplicate last point
                    if (!seg.isEmpty()) {
                        LngLat last = seg.getLast();
                        seg.add(LngLat.builder().lng(last.getLng()).lat(last.getLat()).build());
                    }
                } else {
                    // middle deliveries: start at the previous delivery, go to this delivery
                    start = deliveries.get(i - 1).getDelivery();
                    end = curr.getDelivery();
                    legsPlanned[0]++;
                    seg = new ArrayList<>(safeFindPath(start, end, restrictedAreas, requestId, aStarInvocations, straightLineFallbacks, legsFailed));
                    logger.info("Drone {}: seg size (middle) from findPath(start={}, end={}) = {} (requestId={})", droneId, start, end, seg.size(), requestId);

                    // hover at end -> duplicate last point
                    if (!seg.isEmpty()) {
                        LngLat last = seg.getLast();
                        seg.add(LngLat.builder().lng(last.getLng()).lat(last.getLat()).build());
                    }
                }

                // compute moves and cost for this delivery
                int moves = Math.max(0, seg.size() - 1);
                totalMoves += moves;

                Drone assignedDrone = droneById.get(droneId);
                double cost = 0.0;
                if (assignedDrone != null) {
                    double costPerMove = assignedDrone.getCostPerMove();
                    double costInitial = assignedDrone.getCostInitial();
                    double costFinal = assignedDrone.getCostFinal();
                    cost = costInitial + costFinal + costPerMove * moves;
                }
                totalCost += cost;

                Deliveries del = Deliveries.builder()
                        .deliveryId(curr.getId())
                        .flightPath(seg.toArray(new LngLat[0]))
                        .build();
                droneDeliveries.add(del);
            }

            // build DronePaths for this drone
            if (droneDeliveries.isEmpty()) {
                logger.info("Drone {} produced no droneDeliveries (skipping)", droneId);
                continue;
            }

            DronePaths dp = DronePaths.builder()
                    .droneId(Integer.valueOf(droneId))
                    .deliveries(droneDeliveries.toArray(new Deliveries[0]))
                    .build();
            dronePaths.add(dp);
            logger.info("Added DronePaths for drone {} with {} deliveries (this includes return home)", droneId, droneDeliveries.size());
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
        if (reasonCode == null) {
            reasonCode = (legsFailed[0] > 0) ? "PLANNED_WITH_WARNINGS" : "OK";
        }

        logger.info("calcDeliveryPath finished: requestId={}, durationMs={}, totalMoves={}, totalCost={}, dronePathsCount={}, legsPlanned={}, legsFailed={}, aStarInvocations={}, straightLineFallbacks={}",
                requestId, durationMs, totalMoves, totalCost, dronePaths.size(), legsPlanned[0], legsFailed[0], aStarInvocations[0], straightLineFallbacks[0]);

        PlanningDiagnostics diag = PlanningDiagnostics.builder()
                .requestId(requestId)
                .durationMs(durationMs)
                .reasonCode(reasonCode)
                .legsPlanned(legsPlanned[0])
                .legsFailed(legsFailed[0])
                .aStarInvocations(aStarInvocations[0])
                .straightLineFallbacks(straightLineFallbacks[0])
                .build();

        return CalculatedDeliveryPathRequest.builder()
                .totalCost(totalCost)
                .totalMoves(totalMoves)
                .dronePaths(dronePaths.toArray(new DronePaths[0]))
                .diagnostics(diag)
                .build();
    }

    public ObjectNode buildGeoJsonObject(CalculatedDeliveryPathRequest calc) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("type", "FeatureCollection");
        ArrayNode features = mapper.createArrayNode();

        if (calc == null || calc.getDronePaths() == null || calc.getDronePaths().length == 0) {
            root.set("features", features);
            return root;
        }

        for (DronePaths dp : calc.getDronePaths()) {
            if (dp == null || dp.getDeliveries() == null) continue;
            ObjectNode feat = mapper.createObjectNode();
            feat.put("type", "Feature");

            ObjectNode geometry = mapper.createObjectNode();
            geometry.put("type", "LineString");
            ArrayNode coords = mapper.createArrayNode();

            ArrayNode deliveryIds = mapper.createArrayNode();
            double totalDistance = 0.0;
            int totalMoves = 0;

            List<LngLat> allPoints = new ArrayList<>();

            for (Deliveries d : dp.getDeliveries()) {
                deliveryIds.add(String.valueOf(d.getDeliveryId()));
                LngLat[] path = d.getFlightPath();
                Collections.addAll(allPoints, path);
            }

            for (int i = 0; i < allPoints.size(); i++) {
                LngLat p = allPoints.get(i);
                ArrayNode coord = mapper.createArrayNode();
                coord.add(p.getLng());
                coord.add(p.getLat());
                coords.add(coord);
                if (i > 0) {
                    LngLat prev = allPoints.get(i - 1);
                    totalDistance += Distance.calculateEuclideanDistance(prev, p);
                    totalMoves += 1;
                }
            }

            geometry.set("coordinates", coords);
            feat.set("geometry", geometry);

            ObjectNode props = mapper.createObjectNode();
            props.put("droneId", String.valueOf(dp.getDroneId()));
            props.put("totalDistance", totalDistance);
            props.put("totalMoves", totalMoves);

            if (calc.getDronePaths().length == 1) {
                props.put("totalMoves", calc.getTotalMoves());
                props.put("totalCost", calc.getTotalCost());
            }

            props.set("deliveryIds", deliveryIds);
            feat.set("properties", props);

            features.add(feat);
        }

        root.set("features", features);
        return root;
    }

    // Wrapper that runs pathfinding off-thread with a timeout to avoid blocking the request thread.
    private List<LngLat> safeFindPath(LngLat start,
                                     LngLat goal,
                                     List<RestrictedArea> restrictedAreas,
                                     String requestId,
                                     int[] aStarInvocations,
                                     int[] straightLineFallbacks,
                                     int[] legsFailed) {
        if (start == null || goal == null) {
            legsFailed[0]++;
            logger.warn("safeFindPath: null start/goal (requestId={}) start={} goal={}", requestId, start, goal);
            return List.of();
        }

        aStarInvocations[0]++;
        Callable<List<LngLat>> task = () -> FlightPathAlgorithm.findPath(start, goal, restrictedAreas);
        Future<List<LngLat>> fut = PATH_EXECUTOR.submit(task);
        try {
            // 180s timeout per leg (allow longer A* runs but still bounded)
            return fut.get(180, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            straightLineFallbacks[0]++;
            legsFailed[0]++;
            logger.warn("safeFindPath timeout between {} and {} (requestId={}) - using straight-line fallback", start, goal, requestId);
            fut.cancel(true);
            return FlightPathAlgorithm.getStraightlinePath(start, goal, Math.max(1, (int) (Distance.calculateEuclideanDistance(start, goal) / 1.5E-4)));
        } catch (InterruptedException ie) {
            straightLineFallbacks[0]++;
            legsFailed[0]++;
            Thread.currentThread().interrupt();
            logger.warn("safeFindPath interrupted between {} and {} (requestId={}) - using straight-line fallback", start, goal, requestId);
            return FlightPathAlgorithm.getStraightlinePath(start, goal, Math.max(1, (int) (Distance.calculateEuclideanDistance(start, goal) / 1.5E-4)));
        } catch (ExecutionException ee) {
            straightLineFallbacks[0]++;
            legsFailed[0]++;
            logger.warn("safeFindPath execution error between {} and {} (requestId={}): {} - using straight-line fallback", start, goal, requestId, ee.toString());
            return FlightPathAlgorithm.getStraightlinePath(start, goal, Math.max(1, (int) (Distance.calculateEuclideanDistance(start, goal) / 1.5E-4)));
        }
    }
}
