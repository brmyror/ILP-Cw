package uk.ac.ed.acp.cw2.controller;

import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.Distance;
import uk.ac.ed.acp.cw2.data.Position;
import uk.ac.ed.acp.cw2.data.RuntimeEnvironment;

import java.net.URL;
import java.time.Instant;
import java.util.Map;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
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
     * @param positions A map containing two Position objects with keys "position1" and "position2".
     * @return The Euclidean distance between the two positions, or a 400 Bad Request status if the input is invalid.
     */

    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(@RequestBody Map<String, Position> positions) {
        try {
            Position p1 = positions.get("position1");
            Position p2 = positions.get("position2");
            if (p1 == null || p2 == null) {
                logger.error("bad request");
                return ResponseEntity.status(400).build();
            }
            double distance = Distance.calculateEuclideanDistance(p1, p2);
            return ResponseEntity.ok(distance);
        } catch (Exception e) {
            logger.error("bad request", e);
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Endpoint to determine if two geographical positions are within a proximity.
     * Expects a JSON payload with two positions, each containing latitude and longitude.
     * The threshold for being "within a proximity" is defined as a Euclidean distance of less than 0.00015.
     *
     * @param positions A map containing two Position objects with keys "position1" and "position2".
     * @return A boolean indicating if the two positions are close, or a 400 Bad Request status if the input is invalid.
     */

    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody Map<String, Position> positions) {
        try {
            Position p1 = positions.get("position1");
            Position p2 = positions.get("position2");
            if (p1 == null || p2 == null) {
                logger.error("bad request");
                return ResponseEntity.status(400).build();
            }
            double distance = Distance.calculateEuclideanDistance(p1, p2);
            return ResponseEntity.ok(distance < 0.00015);
        } catch (Exception e) {
            logger.error("bad request", e);
            return ResponseEntity.status(400).build();
        }
    }
}
