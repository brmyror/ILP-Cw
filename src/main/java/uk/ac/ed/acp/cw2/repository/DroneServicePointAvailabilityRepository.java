package uk.ac.ed.acp.cw2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.ac.ed.acp.cw2.entity.DroneServicePointAvailability;

import java.util.UUID;

@Repository
public interface DroneServicePointAvailabilityRepository extends JpaRepository<DroneServicePointAvailability, UUID>, JpaSpecificationExecutor<DroneServicePointAvailability> {
}
