package uk.ac.ed.acp.cw2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Position {
    @JsonProperty("lng")
    private Double lng;

    @JsonProperty("lat")
    private Double lat;

    public static Boolean errorHandler(Position pos) {
        // Check if pos is null
        if (pos == null) {
            return true;
        }

        // Check if lng or lat is null
        else if (pos.getLng() == null || pos.getLat() == null) {
            return true;
        }

        // Longitude must be between -180 and 180,
        else if (pos.getLng() > 180 || pos.getLng() < -180) {
            return true;
        }

        // Latitude must be between -90 and 90
        else return pos.getLat() > 90 || pos.getLat() < -90;
    }
}