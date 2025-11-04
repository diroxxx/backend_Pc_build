package org.example.backend_pcbuild.Offer;


import lombok.AllArgsConstructor;
import org.example.backend_pcbuild.Component.ComponentController;
import org.example.backend_pcbuild.Component.dto.BaseItemDto;
import org.example.backend_pcbuild.Offer.dto.BaseOfferDto;
import org.example.backend_pcbuild.Offer.service.OfferService;
import org.example.backend_pcbuild.models.ItemCondition;
import org.example.backend_pcbuild.models.ItemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://127.0.0.1:5000")
@AllArgsConstructor
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;

    @GetMapping()
//    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String,List<?>>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/shops")
    public ResponseEntity<List<String>> getAllShops() {
        return ResponseEntity.ok(offerService.getAllOfferNames());
    }

    public record OffersPageResponse(
            List<BaseOfferDto> offers,
            boolean hasMore,
            int totalPages,
            long totalElements
    ) {}

    @GetMapping("/v2")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<OffersPageResponse> getAllOffersV2(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ItemType itemType,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrize,
            @RequestParam(required = false) Double maxPrize,
            @RequestParam(required = false) ItemCondition itemCondition,
            @RequestParam(required = false) String shopName
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<BaseOfferDto> allOffersV2 = offerService.getAllOffersV2(pageable, itemType, brand, minPrize, maxPrize, itemCondition, shopName);
        OffersPageResponse response = new OffersPageResponse(
                allOffersV2.getContent(),
                allOffersV2.hasNext(),
                allOffersV2.getTotalPages(),
                allOffersV2.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/count")
    public ResponseEntity<Long> getOffersCount() {
        long count = offerService.countAllVisibleOffers();
        return ResponseEntity.ok(count);
    }



}
