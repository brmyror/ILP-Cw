package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Region request containing a name and an array of vertices.
 */
@Getter
@Setter
@Builder
public class RegionRequest {
    @JsonProperty("name")
    private String name;

    @JsonProperty("vertices")
    private LngLatRequest[] vertices;
}
