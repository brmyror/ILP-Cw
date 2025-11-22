package uk.ac.ed.acp.cw2.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.entity.Drone;

import java.util.List;
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

    public Drone droneDetails(String id, java.util.List<Drone> drones) {
        // Find a drone by id, then map to existing Drone shape
        return drones.stream()
                .filter(d -> id.equals(d.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Drone not found"));
    }
}
