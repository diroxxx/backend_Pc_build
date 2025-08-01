package org.example.backend_pcbuild.controller;


import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Services.ComponentService;
import org.example.backend_pcbuild.models.ComponentDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://127.0.0.1:5000")
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final ComponentService componentService;

    @GetMapping(value = "/components",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Object>>> getComponents() {
        Map<String, List<Object>> result = componentService.fetchComponentsAsMap();
        componentService.saveBasedComponents(result);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/offers",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Object>>> getOffers() {
        Map<String, List<Object>> result = componentService.fetchOffersAsMap();
        componentService.saveAllComponents(result);
        return ResponseEntity.ok(result);
    }
}



