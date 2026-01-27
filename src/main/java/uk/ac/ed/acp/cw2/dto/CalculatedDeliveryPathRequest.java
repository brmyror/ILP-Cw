package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.ac.ed.acp.cw2.entity.DronePaths;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculatedDeliveryPathRequest {
    @JsonProperty("totalCost")
    private Double totalCost;

    @JsonProperty("totalMoves")
    private Integer totalMoves;

    @JsonProperty("dronePaths")
    private DronePaths[] dronePaths;

    // Optional diagnostics for testing/performance analysis.
    @JsonProperty("diagnostics")
    private PlanningDiagnostics diagnostics;
}