package org.project.backend_pcbuild.pcComponents.controller;
import lombok.RequiredArgsConstructor;
import org.project.backend_pcbuild.configuration.ImportCsvFilesService;
import org.project.backend_pcbuild.pcComponents.service.ComponentService;
import org.project.backend_pcbuild.pcComponents.dto.BaseItemDto;
import org.project.backend_pcbuild.pcComponents.dto.GameFpsComponentsFormDto;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.desktop.UserSessionEvent;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;
    private final ImportCsvFilesService importCsvFilesService;
    public record ComponentsPageResponse(
            List<BaseItemDto> items,
            boolean hasMore,
            int totalPages,
            long totalElements
    ) {}

    @GetMapping
    public ResponseEntity<ComponentsPageResponse> getComponents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ComponentType componentType,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String brand) {

        Pageable pageable = PageRequest.of(page, size);
        Page<BaseItemDto> componentsPage = componentService.getComponents(pageable, componentType, brand, searchTerm);

        ComponentsPageResponse response = new ComponentsPageResponse(
                componentsPage.getContent(),
                componentsPage.hasNext(),
                componentsPage.getTotalPages(),
                componentsPage.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> saveComponent(@RequestBody BaseItemDto item) {
        componentService.saveComponent(item);
        return ResponseEntity.ok("Component has been successfully saved");
    }



//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/brands")
    public ResponseEntity<List<String>> getBrands() {
        List<String> allBrands = componentService.getAllBrands();
        System.out.println(allBrands.size());
        if (allBrands.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(componentService.getAllBrands());
    }


    @GetMapping("/fps")
    public ResponseEntity<GameFpsComponentsFormDto> getGameFpsComponents() {
        return ResponseEntity.ok(componentService.getFpsComponents());
    }

    @GetMapping("/cpus")
    public ResponseEntity<?>  getAllCpus() {

        return ResponseEntity.ok(componentService.getCpus());
    }

    @GetMapping("/gpuModels")
    public ResponseEntity<?>  getAllGpuModels() {

        return ResponseEntity.ok(componentService.getGpusModels());
    }

    @GetMapping("/amount")
    public ResponseEntity<?> getAmountOfComponents() {
        return ResponseEntity.ok(componentService.amountOfComponents());
    }

//    @GetMapping("/brands")
//    public ResponseEntity<List<String>> getAllComponentsBrandsNames() {
//        return ResponseEntity.ok(componentService.getAllBrands());
//
//    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Integer> importCsv(@RequestPart("file") MultipartFile file, @RequestParam("componentType") ComponentType componentType) {
        try{
            Integer imported = importCsvFilesService.importComponentsFromCsv(file,componentType);
            return ResponseEntity.ok(imported);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/components-pc-stats")
    public ResponseEntity<?> getComponentsPcStats() {
        return ResponseEntity.ok(componentService.getComponentsPcStats());
    }

}
