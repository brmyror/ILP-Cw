package uk.ac.ed.acp.cw2.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.dto.*;
import uk.ac.ed.acp.cw2.service.*;

import java.net.URL;


@RestController()
@RequestMapping("/api/v1")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Value("${ilp.service.url}")
    public URL serviceUrl;


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
     * @param req A NextPosition object containing a starting Position and an angle in degrees.
     * @param response HttpServletResponse to set the status code.
     * @return A Position object representing the new position, or a 400 Bad Request status if the input is invalid.
     */
    @PostMapping("/nextPosition")
    public LngLat nextPosition(@RequestBody NextPosition req, HttpServletResponse response) {
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
     * @param req An IsInRegion object containing a Position and a Region.
     * @param response HttpServletResponse to set the status code.
     * @return A Boolean indicating if the position is inside the region, or a 400 Bad Request status if the input is invalid.
     */
    @PostMapping("/isInRegion")
    public Boolean isInRegion(@RequestBody IsInRegion req, HttpServletResponse response) {
        return IsInRegionService.isInRegion(req, response, logger);
    }

    /**
    @GetMapping("/dronesWithCooling/{state}")
    public DroneID[] dronesWithCooling(@PathVariable String state) {
        return DronesWithCoolingService.dronesWithCooling(state, logger);
    }

    @GetMapping("/droneDetails/{id}")
    public Drone droneDetails(@PathVariable String id) {
        return DroneDetailsService.droneDetails(id, logger);
    }
    **/
}
