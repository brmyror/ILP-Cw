package uk.ac.ed.acp.cw2.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.Distance;
import uk.ac.ed.acp.cw2.data.LngLatPairRequest;
import uk.ac.ed.acp.cw2.data.Position;

import java.net.URL;
import java.util.Map;


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
     *
     * @param req A LngLatPairRequest object containing two Position objects.
     * @return The Euclidean distance as a Double, or a 400 Bad Request status if the input is invalid.
     */

    @PostMapping("/distanceTo")
    public Double distanceTo(@RequestBody LngLatPairRequest req, HttpServletResponse response) {
        try {
            if (req == null || req.getPosition1() == null || req.getPosition2() == null) {
                response.setStatus(400);
                logger.error("Invalid parameters passed in");
                return null;
            }
            double distance = Distance.calculateEuclideanDistance(req.getPosition1(), req.getPosition2());
            ResponseEntity.ok(distance);
            return distance;
        } catch (Exception e) {
            response.setStatus(400);
            logger.error("bad request", e);
            return null;
        }
    }

    /**
     * Endpoint to determine if two geographical positions are "close" to each other.
     * Uses the distanceTo method to calculate the distance and checks if it is below a certain threshold.
     *
     * @param req A LngLatPairRequest object containing two Position objects.
     * @return A Boolean indicating if the positions are close, or a 400 Bad Request status if the input is invalid.
     */

    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody LngLatPairRequest req, HttpServletResponse response) {
        try {
            double distance = distanceTo(req, response);
            return ResponseEntity.ok(distance < 0.00015);
        } catch (Exception e) {
            logger.error("bad request", e);
            return ResponseEntity.status(400).build();
        }
    }
}
