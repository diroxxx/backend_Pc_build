package org.example.backend_pcbuild.retriveComponentsFromScrapper;

import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Services.ComponentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://127.0.0.1:5000")
@RequestMapping("/collectData")
@AllArgsConstructor
public class RetrieveFromScrapperController {

    private final ComponentService componentService;

//
//    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<Map<String, List<Object>>> getComponents() {
//        Map<String, List<Object>> result = componentService.fetchComponentsAsMap();
//        componentService.saveAllComponents(result);
//        return ResponseEntity.ok(result);
//    }

}
