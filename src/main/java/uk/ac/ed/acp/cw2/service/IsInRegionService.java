package uk.ac.ed.acp.cw2.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import uk.ac.ed.acp.cw2.data.ErrorHandler;
import uk.ac.ed.acp.cw2.dto.IsInRegionRequest;
import uk.ac.ed.acp.cw2.dto.LngLatRequest;
import uk.ac.ed.acp.cw2.dto.RegionRequest;
import org.slf4j.Logger;


import java.awt.*;

@Service
public class IsInRegionService {
    public static Boolean isInRegion(IsInRegionRequest req, HttpServletResponse response, Logger logger) {
        try {
            Boolean errorHandlerIsInRegionRequest = ErrorHandler.isInRegionRequest(req, logger);
            // Validate input, reject if: req, pos, region, lng, lat is NaN or out of bounds
            if (errorHandlerIsInRegionRequest) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.sendError(400);
                logger.error("Invalid position or region parameters passed in \n");
                return null;
            }

            LngLatRequest pos = req.getLngLatRequest();
            RegionRequest regionRequest = req.getRegion();

            // Create a Polygon object from the region's vertices
            Polygon polygon = new Polygon();
            for (LngLatRequest vertex : regionRequest.getVertices()) {
                /*
                  Creates a polygon with the given vertices, scaled to avoid floating point precision issues, cast type
                  int as polygon class requires int parameters, but all coordinates in this project seem to be given
                  and on piazza its said, 6 degrees of precision is enough, and when type cast to int after multiplying by
                  1,000,000, we still have 6 digits after decimal point preserved.
                  https://docs.oracle.com/javase/8/docs/api/java/awt/Polygon.html
                 */
                polygon.addPoint((int) (vertex.getLng() * 1_000_000), (int) (vertex.getLat() * 1_000_000));

                if (vertex.getLat().equals(pos.getLat()) && vertex.getLng().equals(pos.getLng())) {
                    // If the position is exactly one of the vertices, it is considered inside, as its on an edge
                    // result of an edge case found while testing
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
            logger.error("Exception caught \n", e);
            return null;
        }
    }
}

