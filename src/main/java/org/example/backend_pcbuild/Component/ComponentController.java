package org.example.backend_pcbuild.Component;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Component.dto.BaseItemDto;
import org.example.backend_pcbuild.models.ComponentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;
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
}
