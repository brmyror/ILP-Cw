package uk.ac.ed.acp.cw2.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.ac.ed.acp.cw2.dto.LngLat;

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

    @Getter
    @Setter
    @Builder
    public static class limits {
        @NotNull
        private Integer lower;
        @NotNull
        private Integer upper;
    }

    @NotNull
    private LngLat[] vertices;
}