package uk.ac.ed.acp.cw2.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RestrictedArea {
    @NotNull
    private String name;

    @NotNull
    private Integer id;

    @NotNull
    private limits limits;

    private vertices[] vertices;

    @Getter
    @Setter
    @Builder
    public static class limits {
        @NotNull
        private Integer lower;
        @NotNull
        private Integer upper;
    }

    @Getter
    @Setter
    @Builder
    public static class vertices {
        @NotNull
        private Double lng;
        @NotNull
        private Double lat;
    }
}