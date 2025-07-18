package org.example.backend_pcbuild.retriveComponentsFromScrapper;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@CrossOrigin("http://127.0.0.1:5000")
@RequestMapping("/collectData")
@AllArgsConstructor
public class RetrieveFromScrapperController {

    private final RestClient restClient = RestClient.create();



    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMessage(){
        String result = restClient.get()
                .uri("http://127.0.0.1:5000/comp")
                .retrieve()
                .body(String.class);
        System.out.println(result);

        return ResponseEntity.ok(result);
    }

}
