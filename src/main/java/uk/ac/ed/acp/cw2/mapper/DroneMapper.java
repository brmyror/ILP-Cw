package uk.ac.ed.acp.cw2.mapper;

import uk.ac.ed.acp.cw2.dto.DroneDto;
import uk.ac.ed.acp.cw2.entity.Drone;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public final class DroneMapper {
    private DroneMapper() {}

    public static Drone fromDto(DroneDto dto) {
        DroneDto.Capabilities cap = dto.capability();
        return Drone.builder()
                .id(dto.id())
                .name(dto.name())
                .cooling(cap.cooling())
                .heating(cap.heating())
                .capacity(cap.capacity())
                .maxMoves(cap.maxMoves())
                .costPerMove(cap.costPerMove())
                .costInitial(cap.costInitial())
                .costFinal(cap.costFinal())
                .build();
    }

    public static List<Drone> fromDtoList(DroneDto[] dtos) {
        return Arrays.stream(dtos)
                .map(DroneMapper::fromDto)
                .collect(Collectors.toList());
    }
}
