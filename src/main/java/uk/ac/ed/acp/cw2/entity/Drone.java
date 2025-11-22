package uk.ac.ed.acp.cw2.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class Drone {

    @NotNull
    private String id;

    @NotNull
    private String name;

    @NotNull
    private Boolean cooling;

    @NotNull
    private Boolean heating;

    @NotNull
    private Double capacity;

    @NotNull
    private Integer maxMoves;

    @NotNull
    private Double costPerMove;

    @NotNull
    private Double costInitial;

    @NotNull
    private Double costFinal;
}
