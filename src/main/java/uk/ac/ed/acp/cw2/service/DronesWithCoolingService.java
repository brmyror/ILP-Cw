package uk.ac.ed.acp.cw2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.dto.DroneDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DronesWithCoolingService {

    private final DroneService droneService;

    public String[] dronesWithCooling(String state) {
        boolean desiredState = Boolean.parseBoolean(state);
        List<String> ids = droneService.getDrones().stream()
                .filter(d -> Boolean.TRUE.equals(d.capability().cooling()) == desiredState)
                .map(DroneDto::id)
                .toList();
        return ids.toArray(new String[0]);
    }
}
