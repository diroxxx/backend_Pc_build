package org.example.backend_pcbuild.Admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.OfferShopUpdateInfoDto;
import org.example.backend_pcbuild.Admin.dto.OfferUpdateStatsDTO;
import org.example.backend_pcbuild.Admin.repository.OfferUpdateRepository;
import org.example.backend_pcbuild.Admin.service.OfferUpdateService;
import org.example.backend_pcbuild.Offer.service.OfferService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/offers/updates")
@RequiredArgsConstructor
public class OfferUpdateController {
    private final OfferService offerService;
    private final OfferUpdateService offerUpdateService;

//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping()
    public ResponseEntity<List<OfferShopUpdateInfoDto>> getOffersUpdates() {

        return ResponseEntity.ok(offerUpdateService.getOfferUpdates());
    }

//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<List<OfferUpdateStatsDTO>> getOfferUpdateStats() {
    return ResponseEntity.ok(offerUpdateService.getOfferStatsLast30Days());
    }

//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("stats/shops")
    public ResponseEntity<List<OfferUpdateRepository.OfferUpdateShopsOffersAmountStatsProjection>> getOffersShopsAmountStats() {
        return ResponseEntity.ok(offerUpdateService.getOffersShopsAmountStats());
    }

}
