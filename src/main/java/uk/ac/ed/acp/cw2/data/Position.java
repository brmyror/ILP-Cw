package uk.ac.ed.acp.cw2.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Position {
    private double lng;
    private double lat;

    public Position() {
    }

    public Position(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }
}
