package org.example.backend_pcbuild.Components;


import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Services.ComponentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://127.0.0.1:5000")
@AllArgsConstructor
@RequestMapping("/components")
public class ComponentController {

    private final ComponentService componentService;

    @GetMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String,List<?>>> getAllComponents() {
        System.out.println(componentService.getAllComponents());
        return ResponseEntity.ok(componentService.getAllComponents());
    }
}
