package uk.ac.ed.acp.cw2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.ac.ed.acp.cw2.entity.DroneServicePoint;

@Repository
public interface DroneServicePointRepository extends JpaRepository<DroneServicePoint, Integer>, JpaSpecificationExecutor<DroneServicePoint> {
}
