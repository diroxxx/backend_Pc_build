package org.project.backend_pcbuild.offer.controller;


import lombok.AllArgsConstructor;
import org.project.backend_pcbuild.offer.dto.BaseOfferDto;
import org.project.backend_pcbuild.offer.dto.ComponentStatsDto;
import org.project.backend_pcbuild.offer.service.OfferService;
import org.project.backend_pcbuild.pcComponents.model.ComponentCondition;
import org.project.backend_pcbuild.pcComponents.model.ComponentType;
import org.project.backend_pcbuild.offer.model.SortByOffers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("http://127.0.0.1:5000")
@AllArgsConstructor
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;

//    @GetMapping()
//    public ResponseEntity<Map<String,List<?>>> getAllOffers() {
//        return ResponseEntity.ok(offerService.getAllOffers());
//    }

    @GetMapping("/shops")
    public ResponseEntity<List<String>> getAllShops() {
        return ResponseEntity.ok(offerService.getAllOfferNames());
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ComponentStatsDto>> getCountsOffersByComponents() {

        List<ComponentStatsDto> countsOffersByComponents = offerService.getCountsOffersByComponents();
        return ResponseEntity.ok(countsOffersByComponents);
    }

    public record OffersPageResponse(
            List<BaseOfferDto> offers,
            boolean hasMore,
            int totalPages,
            long totalElements
    ) {}

//    @GetMapping
//    public ResponseEntity<OffersPageResponse> getAllOffers(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) ComponentType componentType,
//            @RequestParam(required = false) String brand,
//            @RequestParam(required = false) Double minPrize,
//            @RequestParam(required = false) Double maxPrize,
//            @RequestParam(required = false) ComponentCondition componentCondition,
//            @RequestParam(required = false) String shopName,
//            @RequestParam(required = false) SortByOffers sortBy,
//            @RequestParam(required = false) String query
//
//    ) {
//
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<BaseOfferDto> allOffersV2 = offerService.getAllOffersV2(pageable, componentType, brand, minPrize, maxPrize, componentCondition, shopName, sortBy, query);
//        OffersPageResponse response = new OffersPageResponse(
//                allOffersV2.getContent(),
//                allOffersV2.hasNext(),
//                allOffersV2.getTotalPages(),
//                allOffersV2.getTotalElements()
//        );
//
//
//        return ResponseEntity.ok(response);
//    }

    @GetMapping
    public ResponseEntity<OffersPageResponse> getAllOffersV2(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ComponentType componentType,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrize,
            @RequestParam(required = false) Double maxPrize,
            @RequestParam(required = false) ComponentCondition componentCondition,
            @RequestParam(required = false) String shopName,
            @RequestParam(required = false) SortByOffers sortBy,
            @RequestParam(required = false) String query

    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        if (sortBy == null) sortBy = SortByOffers.NEWEST;
        switch (sortBy) {
            case CHEAPEST ->  sort = Sort.by(Sort.Direction.ASC, "price");
            case EXPENSIVE -> sort = Sort.by(Sort.Direction.DESC, "price");
            case NEWEST -> sort = Sort.by(Sort.Direction.DESC, "id");
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BaseOfferDto> allOffersV2 = offerService.getAllOffersV3(pageable, componentType, brand, minPrize, maxPrize, componentCondition, shopName, query);
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
