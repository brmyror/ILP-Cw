package uk.ac.ed.acp.cw2.dto;

public record RestrictedAreaDto(String name, Integer id, Limits limits, LngLat[] vertices) {
    public record Limits(Integer lower, Integer upper) {}
}

