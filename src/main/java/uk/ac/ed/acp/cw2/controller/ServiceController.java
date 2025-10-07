package uk.ac.ed.acp.cw2.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.*;

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
     * Endpoint to retrieve a static UUID.
     *
     * @return A hardcoded UUID string.
     */

    @GetMapping("/uid")
    public String uid() {
        return "s2334630";
    }

    /**
     * Endpoint to calculate the Euclidean distance between two geographical positions.
     * Expects a JSON payload with two positions, each containing latitude and longitude.
     *{
     *      *   "position1": {
     *      *     "lng": -3.192473,
     *      *     "lat": 55.946233
     *      *   },
     *      *   "position2": {
     *      *     "lng": -3.192473,
     *      *     "lat": 55.946300
     *      *   }
     *      * }
     * @param req A LngLatPairRequest object containing two Position objects.
     * @return The Euclidean distance as a Double, or a 400 Bad Request status if the input is invalid.
     */

    @PostMapping("/distanceTo")
    public Double distanceTo(@RequestBody LngLatPairRequest req, HttpServletResponse response) {
        try {
            // Validate input
            if (req == null || req.getPosition1() == null || req.getPosition2() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.error("Invalid parameters passed in");
                return null;
            }

            // Calculate and return Euclidean distance
            double distance = Distance.calculateEuclideanDistance(req.getPosition1(), req.getPosition2());
            response.setStatus(HttpServletResponse.SC_OK);
            return distance;

            // Catch any exceptions and return a 400 Bad Request status
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Exception caught", e);
            return null;
        }
    }

    /**
     * Endpoint to determine if two geographical positions are "close" to each other.
     * Uses the distanceTo method to calculate the distance and checks if it is below a certain threshold.
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
     * @return A Boolean indicating if the positions are close, or a 400 Bad Request status if the input is invalid.
     */

    @PostMapping("/isCloseTo")
    public Boolean isCloseTo(@RequestBody LngLatPairRequest req, HttpServletResponse response) {
        try {
            // Use the distanceTo method to get the distance between the two positions and
            // check if its < 0.00015
            Boolean isClose = distanceTo(req, response) < 0.00015;
            response.setStatus(HttpServletResponse.SC_OK);
            return isClose;

            // Catch any exceptions and return a 400 Bad Request status
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Exception caught", e);
            return null;
        }
    }

    @PostMapping("nextPosition")
    public Position nextPosition(@RequestBody NextPositionRequest req, HttpServletResponse response) {
        try {
            // Validate input
            if (req == null || req.getStart() == null || req.getAngle() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                logger.error("Invalid parameters passed in");
                return null;
            }

            return null; // TODO implement this method

            // Catch any exceptions and return a 400 Bad Request status
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Exception caught", e);
            return null;
        }
    }
}
