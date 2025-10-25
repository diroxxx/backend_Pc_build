package org.example.backend_pcbuild.Component;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Component.dto.BaseItemDto;
import org.example.backend_pcbuild.Offer.dto.BaseOfferDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {
    private final ComponentService componentService;


    @GetMapping()
    public ResponseEntity<List<BaseItemDto>> getAllComponents() {
        return ResponseEntity.ok(componentService.getAllComponents());
    }

}
