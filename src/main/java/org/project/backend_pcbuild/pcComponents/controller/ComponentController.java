package org.project.backend_pcbuild.pcComponents.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.backend_pcbuild.configuration.ImportCsvFilesService;
import org.project.backend_pcbuild.pcComponents.dto.*;
import org.project.backend_pcbuild.pcComponents.service.ComponentService;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.desktop.UserSessionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
@Slf4j
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
        log.info("Saving component: {}", item);
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

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{componentId}")
    public ResponseEntity<?> updateComponent(@PathVariable Integer componentId, @RequestBody BaseItemDto item) {

        if (componentId == null || item == null) {
            return ResponseEntity.badRequest().build();
        }

        if (item.getComponentType() == null) {
            if (item instanceof ProcessorItemDto) {
                item.setComponentType(ComponentType.PROCESSOR);
            } else if (item instanceof GraphicsCardItemDto) {
                item.setComponentType(ComponentType.GRAPHICS_CARD);
            } else if (item instanceof MotherboardItemDto) {
                item.setComponentType(ComponentType.MOTHERBOARD);
            } else if (item instanceof MemoryItemDto) {
                item.setComponentType(ComponentType.MEMORY);
            } else if (item instanceof PowerSupplyItemDto) {
                item.setComponentType(ComponentType.POWER_SUPPLY);
            } else if (item instanceof CoolerItemDto) {
                item.setComponentType(ComponentType.CPU_COOLER);
            } else if (item instanceof CaseItemDto) {
                item.setComponentType(ComponentType.CASE_PC);
            } else if (item instanceof StorageItemDto) {
                item.setComponentType(ComponentType.STORAGE);
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "componentType missing and cannot be inferred"));
            }

        }
        componentService.updateComponent(componentId, item);
        return ResponseEntity.ok(Map.of("message", "Komponent został zaktualizowany"));

    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{componentId}")
    public ResponseEntity<?> deleteComponent(@PathVariable Integer componentId) {
        if (componentId == null) {
            return ResponseEntity.badRequest().build();
        }
        componentService.deleteComponent(componentId);
        return ResponseEntity.ok(Map.of("message", "Komponent został usunięty"));

    }
    }
