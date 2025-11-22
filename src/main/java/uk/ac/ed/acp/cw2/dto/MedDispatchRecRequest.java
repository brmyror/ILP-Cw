package uk.ac.ed.acp.cw2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedDispatchRecRequest {
    @JsonProperty("id")
    @NotNull
    private Integer id;

    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("time")
    private LocalTime time;

    @JsonProperty("requirements")
    private Requirements requirements;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Requirements {
        @JsonProperty("capacity")
        @NotNull
        private Double capacity;

        @JsonProperty("cooling")
        private boolean cooling;

        @JsonProperty("heating")
        private boolean heating;

        @JsonProperty("maxCost")
        private Double maxCost;
    }

    @JsonProperty("delivery")
    private LngLat delivery;

    // TODO figure out intake of requests split by AND
}
//{
//  "id": 123,
//  "date": "2025-12-22",
//  "time": "14:30",
//  "requirements": {
//    "capacity": 0.75,
//    "cooling": false,
//    "heating": true,
//    "maxCost": 13.5
//  },
//
//  "delivery": {
//    	"lng": -3.00
//    	"lat": 55.121
//  }
//}
//AND
//...
