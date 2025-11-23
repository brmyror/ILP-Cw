package uk.ac.ed.acp.cw2.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.entity.Drone;
import uk.ac.ed.acp.cw2.entity.DroneForServicePoint;
import uk.ac.ed.acp.cw2.service.*;

import java.net.URL;
import java.util.List;


@RestController()
@RequestMapping("/api/v1")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Value("${ilp.service.url}")
    public URL serviceUrl;

    private final ILPRestController ilpRestController;
    private final DroneService droneService;

    @Autowired
    public ServiceController(ILPRestController ilpRestController,
                             DroneService droneService) {
        this.ilpRestController = ilpRestController;
        this.droneService = droneService;
    }


    @GetMapping("/")
    public String index() {
        return "<html><body>" +
                "<h1>Welcome from ILP</h1>" +
                "<h4>ILP-REST-Service-URL:</h4> <a href=\"" + serviceUrl + "\" target=\"_blank\"> " + serviceUrl+ " </a>" +
                "</body></html>";
    }

    /**
     * Endpoint to retrieve a static UID.
     *
     * @return A hardcoded UID string.
     */

    @GetMapping("/uid")
    public String uid() {
        return "s2334630";
    }

    /**
     * Endpoint to calculate the Euclidean distance between two geographical positions.
     * Expects a JSON payload with two positions, each containing latitude and longitude.
     *{
     *   "position1": {
     *    "lng": -3.192473,
     *    "lat": 55.946233
     *  },
     *   "position2": {
     *    "lng": -3.192473,
     *    "lat": 55.946300
     *  }
     * }
     * @param req A LngLatPairRequest object containing two Position objects.
     * @param response HttpServletResponse to set the status code.
     * @return The Euclidean distance as a Double, or a 400 Bad Request status if the input is invalid.
     */

    @PostMapping("/distanceTo")
    public Double distanceTo(@RequestBody PositionPair req, HttpServletResponse response) {
        return DistanceToService.distanceTo(req, response, logger);
    }

    /**
     * Endpoint to determine if two geographical positions are "close" to each other.
     * Uses the distanceTo method to calculate the distance and checks if it is below 0.00015.
     * Accepts JSON like:
     * {
     *   "position1": {
     *     "lng": -3.192473,
     *     "lat": 55.946233
     *   },
     *   "position2": {
     *     "lng": -3.192473,
     *     "lat": 55.946300
     *   }
     * }
     * @param req A LngLatPairRequest object containing two Position objects.
     * @param response HttpServletResponse to set the status code.
     * @return A Boolean indicating if the positions are close, or a 400 Bad Request status if the input is invalid.
     */

    @PostMapping("/isCloseTo")
    public Boolean isCloseTo(@RequestBody PositionPair req, HttpServletResponse response) {
        return IsCloseToService.isCloseTo(req, response, logger);
    }

    /**
     * Endpoint to calculate the next geographical position given a starting position and an angle.
     * The angle must be one of the 16 cardinal points (0, 22.5, 45, ..., 360 degrees).
     * The movement distance is fixed at 0.00015 degrees.
     * Accepts JSON like:
     * {
     *   "start": {
     *     "lng": -3.192473,
     *     "lat": 55.946233
     *   },
     *   "angle": 90
     * }
     * @param req A NextPositionRequest object containing a starting Position and an angle in degrees.
     * @param response HttpServletResponse to set the status code.
     * @return A Position object representing the new position, or a 400 Bad Request status if the input is invalid.
     */
    @PostMapping("/nextPosition")
    public LngLat nextPosition(@RequestBody NextPositionRequest req, HttpServletResponse response) {
        return NextPositionService.nextPosition(req, response, logger);
    }

    /**
     * Endpoint to determine if a given geographical position is inside a specified region.
     * The region is defined by a closed polygon with at least 4 vertices (the first and last vertex must be the same).
     * Accepts JSON like:
     * {
     *   "position": {
     *     "lng": -3.192473,
     *     "lat": 55.946233
     *   },
     *   "region": {
     *     "name": "Test Region",
     *     "vertices": [
     *       {"lng": -3.192000, "lat": 55.946000},
     *       {"lng": -3.193000, "lat": 55.946000},
     *       {"lng": -3.193000, "lat": 55.947000},
     *       {"lng": -3.192000, "lat": 55.947000},
     *       {"lng": -3.192000, "lat": 55.946000}
     *     ]
     *   }
     * }
     * @param req An IsInRegionRequest object containing a Position and a Region.
     * @param response HttpServletResponse to set the status code.
     * @return A Boolean indicating if the position is inside the region, or a 400 Bad Request status if the input is invalid.
     */
    @PostMapping("/isInRegion")
    public Boolean isInRegion(@RequestBody IsInRegionRequest req, HttpServletResponse response) {
        return IsInRegionService.isInRegion(req, response, logger);
    }

    /**
     * Endpoint to retrieve a list of drone IDs that have cooling capabilities based on their state.
     * @param state The state of the drone's cooling capability to retrieve.
     * @return A list of drone IDs, or an empty list if none are found.
     */
    @GetMapping("/dronesWithCooling/{state}")
    public String[] dronesWithCooling(@PathVariable Boolean state) {
        List<Drone> drones = ilpRestController.fetchDronesFromIlp();
        return droneService.dronesWithCooling(state, drones);
    }

    /**
     * Endpoint to return the Drone entity for the given id or throws 404 if not found.
     *
     * @param id drone id
     * @return Drone entity
     * @throws ResponseStatusException with 404 status when the id does not exist
     */
    @GetMapping("/droneDetails/{id}")
    public Drone droneDetails(@PathVariable String id) {
        List<Drone> drones = ilpRestController.fetchDronesFromIlp();
        return droneService.droneDetails(id, drones);
    }

    /**
     * Endpoint to return an array of drone IDs that are able to fulfill the given array of MedDispatchRec.
     * @param req array of MedDispatchRecs
     * @return array of drone IDs, or an empty list if none are found.
     */
    @PostMapping("/queryAvailableDrones")
    public String[] queryAvailableDrones(@RequestBody MedDispatchRecRequest[] req) {
        List<Drone> drones = ilpRestController.fetchDronesFromIlp();
        List<DroneForServicePoint> dronesForServicePoints = ilpRestController.fetchDronesForServicePointsFromIlp();
        return droneService.queryAvailableDrones(req, drones, dronesForServicePoints);
    }

    @GetMapping("/queryAsPath/{attribute-name}/{attribute-value}")
    public String[] queryAsPath(@PathVariable("attribute-name") String name, @PathVariable("attribute-value") String value ) {
        List<Drone> drones = ilpRestController.fetchDronesFromIlp();
        return droneService.queryAsPath(name, value, drones);
    }
}
