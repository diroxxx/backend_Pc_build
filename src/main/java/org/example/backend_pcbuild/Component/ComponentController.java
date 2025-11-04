package org.example.backend_pcbuild.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Component.dto.BaseItemDto;
import org.example.backend_pcbuild.models.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam(required = false) ItemType itemType,
            @RequestParam(required = false) String brand, HttpServletRequest request) {
//        System.out.println(itemType);
//        System.out.println("=== Request Parameters ===");
//        request.getParameterMap().forEach((key, values) -> {
//            System.out.println(key + " = " + Arrays.toString(values));
//        });
        Pageable pageable = PageRequest.of(page, size);
        Page<BaseItemDto> componentsPage = componentService.getComponents(pageable, itemType, brand);

        ComponentsPageResponse response = new ComponentsPageResponse(
                componentsPage.getContent(),
                componentsPage.hasNext(),
                componentsPage.getTotalPages(),
                componentsPage.getTotalElements()
        );

        return ResponseEntity.ok(response);
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
