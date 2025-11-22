package uk.ac.ed.acp.cw2.mapper;

import uk.ac.ed.acp.cw2.dto.DroneServicePointDto;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.entity.DroneServicePoint;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class DroneServicePointMapper {
    private DroneServicePointMapper() {}

    public static DroneServicePoint fromDto(DroneServicePointDto dto) {
        LngLat location = LngLat.builder()
                .lng(dto.longitude())
                .lat(dto.latitude())
                .build();

        return DroneServicePoint.builder()
                .id(dto.id())
                .name(dto.name())
                .location(location)
                .build();
    }

    public static List<DroneServicePoint> fromDtoList(DroneServicePointDto[] dtos) {
        return Arrays.stream(dtos)
                .map(DroneServicePointMapper::fromDto)
                .collect(Collectors.toList());
    }
}
