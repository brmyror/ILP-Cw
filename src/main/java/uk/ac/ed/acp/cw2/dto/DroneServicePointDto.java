package uk.ac.ed.acp.cw2.dto;

/**
 * DTO for DroneServicePoint.
 */
public record DroneServicePointDto(String name, Integer id,  Location location) {
    public record Location(Double lng, Double lat, Integer alt) {
    }
}
