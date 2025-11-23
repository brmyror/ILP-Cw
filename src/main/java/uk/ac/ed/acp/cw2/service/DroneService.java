package uk.ac.ed.acp.cw2.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.data.DynamicQueries;
import uk.ac.ed.acp.cw2.dto.CalculatedDeliveryPathRequest;
import uk.ac.ed.acp.cw2.dto.MedDispatchRecRequest;
import uk.ac.ed.acp.cw2.dto.QueryRequest;
import uk.ac.ed.acp.cw2.entity.Drone;
import uk.ac.ed.acp.cw2.entity.DroneForServicePoint;
import uk.ac.ed.acp.cw2.entity.DroneServicePoint;
import uk.ac.ed.acp.cw2.entity.RestrictedArea;

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
                                                          List<DroneForServicePoint> dronesForServicePoints,
                                                          List<RestrictedArea> restrictedAreas, String[] droneIDs) {

        return null;
    }
}
