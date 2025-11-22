package uk.ac.ed.acp.cw2.mapper;

import uk.ac.ed.acp.cw2.dto.DroneForServicePointDto;
import uk.ac.ed.acp.cw2.entity.DroneForServicePoint;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class DroneForServicePointMapper {
    private DroneForServicePointMapper() {}

    public static DroneForServicePoint fromDto(DroneForServicePointDto dto) {
        DroneForServicePoint.DroneAvailability[] droneAvailabilities = Arrays.stream(dto.drones())
                .map(DroneForServicePointMapper::mapDroneAvailability)
                .toArray(DroneForServicePoint.DroneAvailability[]::new);

        return DroneForServicePoint.builder()
                .servicePointId(dto.servicePointId())
                .drones(droneAvailabilities)
                .build();
    }

    public static List<DroneForServicePoint> fromDtoList(DroneForServicePointDto[] dtos) {
        return Arrays.stream(dtos)
                .map(DroneForServicePointMapper::fromDto)
                .collect(Collectors.toList());
    }

    private static DroneForServicePoint.DroneAvailability mapDroneAvailability(DroneForServicePointDto.DroneAvailability dto) {
        DroneForServicePoint.Availability[] availabilities = Arrays.stream(dto.availability())
                .map(DroneForServicePointMapper::mapAvailability)
                .toArray(DroneForServicePoint.Availability[]::new);


        return DroneForServicePoint.DroneAvailability.builder()
                .id(dto.id())
                .availability(availabilities)
                .build();
    }

    private static DroneForServicePoint.Availability mapAvailability(DroneForServicePointDto.DroneAvailability.Availability dto) {
        return DroneForServicePoint.Availability.builder()
                .dayOfWeek(dto.dayOfWeek())
                .from(dto.from())
                .until(dto.until())
                .build();
    }
}
