package uk.ac.ed.acp.cw2.mapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.ac.ed.acp.cw2.dto.DroneServicePointDto;
import uk.ac.ed.acp.cw2.entity.DroneServicePoint;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DroneServicePointMapper {

    @Mapping(target = "location", expression = "java(new LngLatAlt(servicePoint.getLongitude(), servicePoint.getLatitude(), servicePoint.getAltitude()))")
    DroneServicePointDto toDto(DroneServicePoint servicePoint);

    List<DroneServicePointDto> toDtoList(List<DroneServicePoint> servicePoints);
}
