package uk.ac.ed.acp.cw2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.acp.cw2.dto.DroneDto;
import uk.ac.ed.acp.cw2.dto.DroneForServicePointDto;
import uk.ac.ed.acp.cw2.dto.DroneServicePointDto;
import uk.ac.ed.acp.cw2.dto.RestrictedAreaDto;
import uk.ac.ed.acp.cw2.entity.Drone;
import uk.ac.ed.acp.cw2.entity.DroneForServicePoint;
import uk.ac.ed.acp.cw2.entity.DroneServicePoint;
import uk.ac.ed.acp.cw2.entity.RestrictedArea;
import uk.ac.ed.acp.cw2.mapper.DroneForServicePointMapper;
import uk.ac.ed.acp.cw2.mapper.DroneMapper;
import uk.ac.ed.acp.cw2.mapper.DroneServicePointMapper;
import uk.ac.ed.acp.cw2.mapper.RestrictedAreaMapper;

import java.util.List;

@Controller
public class ILPRestController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment env;

    private final String ilpEndpoint;

    @Autowired
    public ILPRestController(String ilpEndpoint) {
        this.ilpEndpoint = ilpEndpoint;
    }

    // Read the base ILP URL on every call so it can change between requests.
    private String getServiceBaseUrl() {
        // Prefer spring property (allows dynamic changes via Environment) if present, otherwise use injected bean value.
        String url = env.getProperty("ilp.service.url");
        if (url != null && !url.isBlank()) {
            if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
            return url;
        }

        // Fallback to bean resolved at startup (reads env var or default if property missing)
        return ilpEndpoint;
    }

    public List<Drone> fetchDronesFromIlp() {
        String dronesUrl = getServiceBaseUrl() + "/drones";
        DroneDto[] response = restTemplate.getForObject(dronesUrl, DroneDto[].class);
        return DroneMapper.fromDtoList(response);
    }

    public List<DroneServicePoint> fetchServicePointsFromIlp() {
        String servicePointsUrl = getServiceBaseUrl() + "/service-points";
        DroneServicePointDto[] response = restTemplate.getForObject(servicePointsUrl, DroneServicePointDto[].class);
        return DroneServicePointMapper.fromDtoList(response);
    }

    public List<DroneForServicePoint> fetchDronesForServicePointsFromIlp() {
        String forServicePointsUrl = getServiceBaseUrl() + "/drones-for-service-points";
        DroneForServicePointDto[] response = restTemplate.getForObject(forServicePointsUrl, DroneForServicePointDto[].class);
        return DroneForServicePointMapper.fromDtoList(response);
    }

    public List<RestrictedArea> fetchRestrictedAreasFromIlp() {
        String restrictedAreasUrl = getServiceBaseUrl() + "/restricted-areas";
        RestrictedAreaDto[] response = restTemplate.getForObject(restrictedAreasUrl, RestrictedAreaDto[].class);
        return RestrictedAreaMapper.fromDtoList(response);
    }
}
