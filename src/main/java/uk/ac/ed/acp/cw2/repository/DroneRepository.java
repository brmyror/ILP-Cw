package uk.ac.ed.acp.cw2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.ac.ed.acp.cw2.entity.Drone;

@Repository
public interface DroneRepository extends JpaRepository<Drone, String>, JpaSpecificationExecutor<Drone> {
}
