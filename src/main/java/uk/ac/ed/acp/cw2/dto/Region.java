package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Region {
    @JsonProperty("name")
    private String name;

    @JsonProperty("vertices")
    private Position[] vertices;
}
