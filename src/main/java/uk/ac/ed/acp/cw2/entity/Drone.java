package uk.ac.ed.acp.cw2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;


@Getter
@Setter
@Entity
@Table(name = "drones", schema = "ilp")
public class Drone {
    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "cooling", nullable = false)
    private Boolean cooling = false;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "heating", nullable = false)
    private Boolean heating = false;

    @NotNull
    @Column(name = "capacity", nullable = false)
    private Double capacity;

    @NotNull
    @Column(name = "max_moves", nullable = false)
    private Integer maxMoves;

    @NotNull
    @Column(name = "cost_per_move", nullable = false)
    private Double costPerMove;

    @NotNull
    @Column(name = "cost_initial", nullable = false)
    private Double costInitial;

    @NotNull
    @Column(name = "cost_final", nullable = false)
    private Double costFinal;
}
