package uk.ac.ed.acp.cw2.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.data.Distance;
import uk.ac.ed.acp.cw2.data.DynamicQueries;
import uk.ac.ed.acp.cw2.data.FlightPathAlgorithm;
import uk.ac.ed.acp.cw2.dto.CalculatedDeliveryPathRequest;
import uk.ac.ed.acp.cw2.dto.MedDispatchRecRequest;
import uk.ac.ed.acp.cw2.dto.QueryRequest;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.entity.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class DroneService {

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

        // TODO should return []
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
            // TODO: Only perform attribute-based filtering and use a lower-bound or estimated cost such as \
            //(distance(servicePoint, delivery)/step) × costPerMove + costInitial + costFinal, divided by the number of dispatches;

            availableDrones = availableDrones.stream()
                    .filter(d -> {
                        if (date == null && time == null) return true; // no constraint

                        List<DroneForServicePoint.Availability> avails = availabilityMap.get(d.getId());

                        // If date provided, check day-of-week match; if time provided, also check the time window
                        if (date != null) {
                            java.time.DayOfWeek dow = date.getDayOfWeek();
                            return avails.stream().anyMatch(a -> a.getDayOfWeek() == dow &&
                                    (time == null || (!a.getFrom().isAfter(time) && !a.getUntil().isBefore(time))));
                        } else { // date == null but time != null: accept any availability window that contains the time
                            return avails.stream().anyMatch(a -> (!a.getFrom().isAfter(time) && !a.getUntil().isBefore(time)));
                        }
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
                                                          List<RestrictedArea> restrictedAreas, String[] droneIDs) {

        // In case no available drones, return empty response
        if (droneIDs == null || droneIDs.length == 0) return CalculatedDeliveryPathRequest.builder().totalCost(0.0).totalMoves(0).dronePaths(new DronePaths[0]).build();

        // Map drone id -> Drone entity for cost lookups
        Map<String, Drone> droneById = new HashMap<>();
        for (Drone d : drones) droneById.put(d.getId(), d);

        // TODO implement better distribution of requests to drones
        // Evenly distribute requests to drone IDs
        Map<String, List<MedDispatchRecRequest>> assignments = new LinkedHashMap<>();
        for (String id : droneIDs) assignments.put(id, new ArrayList<>());

        int idx = 0;
        for (MedDispatchRecRequest m : req) {
            String assigned = droneIDs[idx % droneIDs.length];
            assignments.get(assigned).add(m);
            idx++;
        }

        int totalMoves = 0;
        double totalCost = 0.0;
        List<DronePaths> dronePaths = new ArrayList<>();

        // For each drone, build sequential flightPaths
        for (String droneId : assignments.keySet()) {
            List<MedDispatchRecRequest> deliveries = assignments.get(droneId);
            if (deliveries.isEmpty()) continue;

            // Determine service point origin for this drone: nearest to first delivery
            DroneServicePoint originSp = servicePoints.getFirst();
            LngLat firstDeliveryPos = deliveries.getFirst().getDelivery();
            double bestDist = Double.MAX_VALUE;
            for (DroneServicePoint sp : servicePoints) {
                double d = Distance.calculateEuclideanDistance(sp.getLocation(), firstDeliveryPos);
                if (d < bestDist) {
                    bestDist = d;
                    originSp = sp;
                }
            }

            List<Deliveries> droneDeliveries = new ArrayList<>();

            // build segments of the total flight path
            for (int i = 0; i < deliveries.size(); i++) {
                MedDispatchRecRequest curr = deliveries.get(i);
                LngLat start;
                LngLat end;
                List<LngLat> seg;

                if (i == deliveries.size() - 1) {
                    // TODO in case where there is only 1 delivery to be made the return flight should be a different element with a null deliveryID
                    // last delivery: start at the previous delivery (or origin if only one), go to last delivery then return to origin
                    start = deliveries.size() == 1 ? originSp.getLocation() : deliveries.get(i - 1).getDelivery();
                    end = curr.getDelivery();
                    List<LngLat> firstLeg = FlightPathAlgorithm.findPath(start, end, restrictedAreas);
                    List<LngLat> returnLeg = FlightPathAlgorithm.findPath(end, originSp.getLocation(), restrictedAreas);
                    // concatenate; since both legs include end, concatenation yields duplicate indicating hover
                    seg = new ArrayList<>(firstLeg);
                    if (!returnLeg.isEmpty()) {
                        seg.addAll(returnLeg);
                    }
                } else if (i == 0) {
                    // start at service point, go to the first delivery
                    start = originSp.getLocation();
                    end = curr.getDelivery();
                    seg = FlightPathAlgorithm.findPath(start, end, restrictedAreas);
                    // hover at end -> duplicate last point
                    if (!seg.isEmpty()) {
                        LngLat last = seg.getLast();
                        seg.add(LngLat.builder().lng(last.getLng()).lat(last.getLat()).build());
                    }
                } else {
                    // middle deliveries: start at the previous delivery, go to this delivery
                    start = deliveries.get(i - 1).getDelivery();
                    end = curr.getDelivery();
                    seg = FlightPathAlgorithm.findPath(start, end, restrictedAreas);
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
            DronePaths dp = DronePaths.builder()
                    .droneId(Integer.valueOf(droneId))
                    .deliveries(droneDeliveries.toArray(new Deliveries[0]))
                    .build();
            dronePaths.add(dp);
        }

        return CalculatedDeliveryPathRequest.builder()
                .totalCost(totalCost)
                .totalMoves(totalMoves)
                .dronePaths(dronePaths.toArray(new DronePaths[0]))
                .build();
    }
}
