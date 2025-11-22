package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.data.ErrorHandler;
import uk.ac.ed.acp.cw2.dto.LngLat;
import uk.ac.ed.acp.cw2.dto.NextPositionRequest;

@Service
public class NextPositionService {
    public static LngLat nextPosition(NextPositionRequest req, HttpServletResponse response, Logger logger) {
        try {
            Boolean errorHandlerNextPosition = ErrorHandler.nextPositionRequest(req, logger);
            // Validate input, reject if: start, angle, lng, lat is NaN or out of bounds
            if (errorHandlerNextPosition) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(400);
                logger.error("Invalid position or angle parameters passed in \n");
                return null;
            }

            // Check that a given angle is one of 16 cardinal points
            double degrees = req.getAngle();

            final double MOVE_DISTANCE = 0.00015;

            // Calculate the change in latitude and longitude based on the angle converted to radians as
            // the trigonometric functions in Java uses radians
            double rad = Math.toRadians(degrees);
            double changeInLng = MOVE_DISTANCE * Math.cos(rad);
            double changeInLat = MOVE_DISTANCE * Math.sin(rad);

            LngLat start = req.getStart();

            response.setStatus(HttpServletResponse.SC_OK);
            // build and return the new Position
            return LngLat.builder().lng(start.getLng() + changeInLng).lat(start.getLat() + changeInLat).build();

            // Catch any other exceptions and return a 400 Bad Request status
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Exception caught \n", e);
            return null;
        }
    }
}
