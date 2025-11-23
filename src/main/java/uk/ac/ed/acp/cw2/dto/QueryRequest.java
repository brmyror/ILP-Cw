package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class QueryRequest {
    @JsonProperty("attribute")
    private String attribute;

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("value")
    private String value;

}