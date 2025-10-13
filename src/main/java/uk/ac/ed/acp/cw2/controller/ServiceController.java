package uk.ac.ed.acp.cw2.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.acp.cw2.data.*;
import java.awt.Polygon;

import java.net.URL;


@RestController()
@RequestMapping("/api/v1")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    public static final Boolean VERBOSE = true;

    public static Logger getLogger() {
        return logger;
    }

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
    public Double distanceTo(@RequestBody LngLatPairRequest req, HttpServletResponse response) {
        try {

            Boolean errorHandlerDistanceTo = LngLatPairRequest.errorHandler(req);
            // Validate input and reject if: req, pos1, pos2, lng, lat is NaN or out of bounds
            if (errorHandlerDistanceTo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(400);
                logger.error("Invalid position parameters passed in");
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
    public Boolean isCloseTo(@RequestBody LngLatPairRequest req, HttpServletResponse response) {
        try {

            Boolean errorHandlerIsCloseTo = LngLatPairRequest.errorHandler(req);
            // Validate input and reject if: req, pos1, pos2, lng, lat is NaN or out of bounds
            if (errorHandlerIsCloseTo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(400);
                logger.error("Invalid position parameters passed in");
                return null;
            }

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
    public Position nextPosition(@RequestBody NextPositionRequest req, HttpServletResponse response) {
        try {
            Boolean errorHandlerNextPosition = NextPositionRequest.errorHandler(req);
            // Validate input, reject if: start, angle, lng, lat is NaN or out of bounds
            if (errorHandlerNextPosition) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(400);
                logger.error("Invalid parameters passed in");
                return null;
            }

            // Check that given angle is one of 16 cardinal points
            double degrees = req.getAngle();

            final double MOVE_DISTANCE = 0.00015;

            // Calculate the change in latitude and longitude based on the angle
            double changeInLng = MOVE_DISTANCE * Math.cos(degrees);
            double changeInLat = MOVE_DISTANCE * Math.sin(degrees);

            Position start = req.getStart();
            Position next = new Position();

            // Calculate the new position
            next.setLng(start.getLng() + changeInLng);
            next.setLat(start.getLat() + changeInLat);
            response.setStatus(HttpServletResponse.SC_OK);
            return next;

            // Catch any other exceptions and return a 400 Bad Request status
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Exception caught", e);
            return null;
        }
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
        try {
            Boolean errorHandlerIsInRegionRequest = IsInRegionRequest.errorHandler(req);
            // Validate input, reject if: req, pos, region, lng, lat is NaN or out of bounds
            if (errorHandlerIsInRegionRequest) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(400);
                logger.error("Invalid parameters passed in");
                return null;
            }

            Position pos = req.getPosition();
            Region region = req.getRegion();

            // Create a Polygon object from the region's vertices
            Polygon polygon = new Polygon();
            for (Position vertice : region.getVertices()) {
               /**
                * Creates a polygon with the given vertices, scaled to avoid floating point precision issues, cast type
                * int as polygon class requires int parameters, does only work for points with 6 decimal places,
                * but all coordinates in this project seem to be given with 6 decimal places, and on piazza its said,
                * 6 degrees of precision is enough
                * https://docs.oracle.com/javase/8/docs/api/java/awt/Polygon.html
                */
                polygon.addPoint((int) (vertice.getLng() * 1_000_000), (int) (vertice.getLat() * 1_000_000));

                if (vertice.equals(req.getPosition())) {
                    // If the position is exactly one of the vertices, it is considered inside
                    response.setStatus(HttpServletResponse.SC_OK);
                    return true;
                }

            }

            // Check if the position is inside the polygon
            boolean isInside = polygon.contains((int) (pos.getLng() * 1_000_000), (int) (pos.getLat() * 1_000_000));

            response.setStatus(HttpServletResponse.SC_OK);
            return isInside;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Exception caught", e);
            return null;
        }
    }
}
