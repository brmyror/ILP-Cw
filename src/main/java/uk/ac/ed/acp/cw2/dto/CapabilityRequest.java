package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CapabilityRequest {
    @JsonProperty("cooling")
    private Boolean cooling;

    @JsonProperty("heating")
    private Boolean heating;

    @JsonProperty("capacity")
    private Double capacity;

    @JsonProperty("maxMoves")
    private Integer maxMoves;

    @JsonProperty("costPerMove")
    private Double costPerMove;

    @JsonProperty("costInitial")
    private Double costInitial;

    @JsonProperty("costFinal")
    private Double costFinal;
}
