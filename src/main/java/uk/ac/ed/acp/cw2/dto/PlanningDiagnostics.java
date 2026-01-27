package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * Optional diagnostics to support testing and performance analysis.
 *
 * This is returned only when the client asks for it (see calcDeliveryPathWithDiagnostics).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanningDiagnostics {

    @JsonProperty("requestId")
    private final String requestId;

    @JsonProperty("durationMs")
    private final Long durationMs;

    @JsonProperty("reasonCode")
    private final String reasonCode;

    @JsonProperty("legsPlanned")
    private final Integer legsPlanned;

    @JsonProperty("legsFailed")
    private final Integer legsFailed;

    @JsonProperty("aStarInvocations")
    private final Integer aStarInvocations;

    @JsonProperty("straightLineFallbacks")
    private final Integer straightLineFallbacks;
}
