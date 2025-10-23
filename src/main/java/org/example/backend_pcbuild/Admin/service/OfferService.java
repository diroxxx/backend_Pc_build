package org.example.backend_pcbuild.Admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.models.Offer;
import org.example.backend_pcbuild.repository.OfferRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OfferService {
    private final OfferRepository offerRepository;


    @Transactional
    public void deleteOffersByUrl(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        offerRepository.deleteByWebsiteUrlIn(urls);
    }

    public void softDeleteByUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        List<Offer> offersToDelete = offerRepository.findAllByWebsiteUrlIn(urls);

        for ( Offer offer : offersToDelete) {
           for ( String url : urls ) {
               if (offer.getWebsiteUrl().equals(url)){
                   offer.setIsVisible(false);
               }
           }
        }
        offerRepository.saveAll(offersToDelete);

    }
}
