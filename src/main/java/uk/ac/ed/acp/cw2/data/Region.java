package uk.ac.ed.acp.cw2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Region {
    @JsonProperty("name")
    private String name;

    @JsonProperty("vertices")
    private Position[] vertices;

    public static Boolean errorHandler(Region region) {
        // Check if name is null or empty
        if (region.getName() == null || region.getName().isEmpty()) {
            return true;
        }

        // Check if vertices is null
        else if (region.getVertices() == null) {
            return true;
        }

        // Check if vertices has less than 4 positions as region must be a closed polygon (can be a triangle + 1 to close)
        else if (region.getVertices().length < 4) {
            return true;
        }

        // Check if the first and last positions are the same
        else if (!region.getVertices()[0].equals(region.getVertices()[region.getVertices().length - 1])) {
            return true;
        }

        // Check if any position in vertices has an error
        for (Position pos : region.getVertices()) {
            if (Position.errorHandler(pos)) {
                return true;
            }
        }
        return false;
    }
}
