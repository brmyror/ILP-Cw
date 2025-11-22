package uk.ac.ed.acp.cw2.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.dto.MedDispatchRecRequest;
import uk.ac.ed.acp.cw2.entity.Drone;
import uk.ac.ed.acp.cw2.entity.DroneForServicePoint;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    //{
    //  "id": 123,
    //  "date": "2025-12-22",
    //  "time": "14:30",
    //  "requirements": {
    //    "capacity": 0.75,
    //    "cooling": false,
    //    "heating": true,
    //    "maxCost": 13.5
    //  },
    //
    //  "delivery": {
    //    	"lng": -3.00
    //    	"lat": 55.121
    //  }
    //}
    //AND
    //...
    public String[] queryAvailableDrones(MedDispatchRecRequest[] req, List<Drone> drones, List<DroneForServicePoint> dronesForServicePoints) {
        List<Drone> availableDrones = drones;

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
                        // robust capacity handling: guard nulls, compare safely and clamp small fp negatives
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

}
