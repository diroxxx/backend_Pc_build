package org.example.backend_pcbuild.Admin;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.OfferShopUpdateInfoDto;
import org.example.backend_pcbuild.Admin.service.OfferService;
import org.example.backend_pcbuild.Admin.service.OfferUpdateService;
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

//    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping()
    public ResponseEntity<List<OfferShopUpdateInfoDto>> getOffersUpdates() {

        return ResponseEntity.ok(offerUpdateService.getOfferUpdates());
    }


}
