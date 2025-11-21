package uk.ac.ed.acp.cw2.mapper;

import org.mapstruct.Mapper;

import uk.ac.ed.acp.cw2.dto.DroneServicePointAvailabilityDto;
import uk.ac.ed.acp.cw2.entity.DroneServicePointAvailability;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DroneServicePointAvailabilityMapper {

    DroneServicePointAvailabilityDto toDto (DroneServicePointAvailability availability);

    List<DroneServicePointAvailabilityDto> toDtoList(List<DroneServicePointAvailability> availabilities);
}
