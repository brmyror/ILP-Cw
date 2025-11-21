package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.data.Distance;
import uk.ac.ed.acp.cw2.data.ErrorHandler;
import uk.ac.ed.acp.cw2.dto.PositionPair;
import org.slf4j.Logger;

@Service
public class IsCloseToService {
    public static Boolean isCloseTo(PositionPair req, HttpServletResponse response, Logger logger) {
        try {

            Boolean errorHandlerIsCloseTo = ErrorHandler.positionPairRequest(req, logger);
            // Validate input and reject if: req, pos1, pos2, lng, lat is NaN or out of bounds
            if (errorHandlerIsCloseTo) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(400);
                logger.error("Invalid position parameters passed in \n");
                return null;
            }

            // Use the Distance data class to get the distance between the two positions and
            // check if its < 0.00015
            double distance = Distance.calculateEuclideanDistance(req.getLngLat1(), req.getLngLat2());
            Boolean isClose = distance < 0.00015;
            response.setStatus(HttpServletResponse.SC_OK);
            return isClose;

            // Catch any exceptions and return a 400 Bad Request status
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Exception caught \n", e);
            return null;
        }
    }
}
