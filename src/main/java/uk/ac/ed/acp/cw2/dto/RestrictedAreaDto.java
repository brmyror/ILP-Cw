package uk.ac.ed.acp.cw2.dto;

public record RestrictedAreaDto(String name, Integer id, Limits limits, Vertices[] vertices) {
    public record Limits(Integer lower, Integer upper) {}
    public record Vertices(Double lng, Double lat) {}
}

