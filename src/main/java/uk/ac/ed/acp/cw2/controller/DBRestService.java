//package uk.ac.ed.acp.cw2.controller;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import uk.ac.ed.acp.cw2.dto.Region;
//import uk.ac.ed.acp.cw2.service.DroneService;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/v1/ilp/db")
//@Slf4j
//public class DBRestService  {
//
//    private final DroneService droneService;
//
//    @GetMapping(value = {"/restricted-areas"})
//    @Override
//    public Region[] restrictedAreas() {
//        return new Region[0];
//    }
//}
