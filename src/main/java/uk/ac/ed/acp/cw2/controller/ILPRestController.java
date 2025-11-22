package uk.ac.ed.acp.cw2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.net.URL;
import java.util.List;

@Controller
public class ILPRestController {

    @Value("${ilp.service.url}")
    public URL serviceUrl;

    @Autowired
    private RestTemplate restTemplate;

    public List<Drone> fetchDronesFromIlp() {
        String dronesUrl = serviceUrl + "/drones";
        DroneDto[] response = restTemplate.getForObject(dronesUrl, DroneDto[].class);
        return DroneMapper.fromDtoList(response);
    }

    public List<DroneServicePoint> fetchServicePointsFromIlp() {
        String servicePointsUrl = serviceUrl + "service-points";
        DroneServicePointDto[] response = restTemplate.getForObject(servicePointsUrl, DroneServicePointDto[].class);
        return DroneServicePointMapper.fromDtoList(response);
    }

    public List<DroneForServicePoint> fetchDronesForServicePointsFromIlp() {
        String forServicePointsUrl = serviceUrl + "drones-for-service-points";
        DroneForServicePointDto[] response = restTemplate.getForObject(forServicePointsUrl, DroneForServicePointDto[].class);
        return DroneForServicePointMapper.fromDtoList(response);
    }

    public List<RestrictedArea> fetchRestrictedAreasFromIlp() {
        String restrictedAreasUrl = serviceUrl + "restricted-areas";
        RestrictedAreaDto[] response = restTemplate.getForObject(restrictedAreasUrl, RestrictedAreaDto[].class);
        return RestrictedAreaMapper.fromDtoList(response);
    }
}
