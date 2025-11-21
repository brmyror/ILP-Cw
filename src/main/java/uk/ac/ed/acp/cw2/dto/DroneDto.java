package uk.ac.ed.acp.cw2.dto;

/**
 * DTO for Drone.
 */
public record DroneDto (String name, String id, Capabilities capability){
        public record Capabilities(Boolean cooling, Boolean heating, Double capacity, Integer maxMoves, Double costPerMove, Double costInitial, Double costFinal) {

        }
    }