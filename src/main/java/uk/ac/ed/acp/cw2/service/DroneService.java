package uk.ac.ed.acp.cw2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import uk.ac.ed.acp.cw2.dto.DroneDto;
import uk.ac.ed.acp.cw2.dto.DroneServicePointDto;
import uk.ac.ed.acp.cw2.entity.Drone;
import uk.ac.ed.acp.cw2.mapper.DroneMapper;
import uk.ac.ed.acp.cw2.mapper.DroneServicePointMapper;
import uk.ac.ed.acp.cw2.repository.DroneRepository;
import uk.ac.ed.acp.cw2.repository.DroneServicePointAvailabilityRepository;
import uk.ac.ed.acp.cw2.repository.DroneServicePointRepository;

import java.util.List;

import static org.springframework.data.jpa.domain.Specification.where;

@Slf4j
@RequiredArgsConstructor
@Service
public class DroneService {
    private final DroneRepository droneRepository;
    private final DroneServicePointRepository servicePointRepository;
    private final DroneServicePointAvailabilityRepository availabilityRepository;

    private final DroneServicePointMapper servicePointMapper;
    private final JdbcTemplate jdbc;
    private final PlatformTransactionManager transactionManager;

    public List<DroneServicePointDto> getServicePoints() {
        var servicePoints = servicePointRepository.findAll();
        return servicePointMapper.toDtoList(servicePoints);
    }

    public List<DroneDto> getDrones() {
        var drones = droneRepository.findAll();
        return drones.stream()
                .map(DroneMapper::toDto)
                .toList();
    }

//    public List<DroneDto> queryDrones(DroneQueryRequest queryRequest) {
//        Specification<Drone> spec = where(null);
//    }
}
