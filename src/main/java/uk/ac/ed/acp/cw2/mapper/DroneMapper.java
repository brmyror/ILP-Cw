package uk.ac.ed.acp.cw2.mapper;

import jakarta.validation.constraints.NotNull;
import uk.ac.ed.acp.cw2.dto.DroneDto;
import uk.ac.ed.acp.cw2.entity.Drone;

import java.util.List;

public final class DroneMapper {
    private DroneMapper() {}

    public static DroneDto toDto(Drone drone) {
        if (drone == null) {return null;}
        return new DroneDto(
                drone.getName(),
                drone.getId(),
                new DroneDto.Capabilities(
                        bool(drone.getCooling()),
                        bool(drone.getHeating()),
                        toDouble(drone.getCapacity()),
                        drone.getMaxMoves(),
                        toDouble(drone.getCostPerMove()),
                        toDouble(drone.getCostInitial()),
                        toDouble(drone.getCostFinal())
                )
        );
    }

//    public static List<DroneDto> toDtoList(List<Drone> drones) {
//    
//    }

    private static Boolean bool(@NotNull Boolean cooling) {
        return cooling;
    }
    private static Double toDouble(@NotNull Double capacity) {
        return capacity;
    }
}
