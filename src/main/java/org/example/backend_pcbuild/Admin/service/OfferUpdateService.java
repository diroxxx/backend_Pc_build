package org.example.backend_pcbuild.Admin.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.OfferShopUpdateInfoDto;
import org.example.backend_pcbuild.Admin.dto.OfferUpdateStatsDTO;
import org.example.backend_pcbuild.Admin.repository.ShopOfferUpdateRepository;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.Admin.repository.OfferUpdateRepository;
import org.example.backend_pcbuild.repository.OfferRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferUpdateService {
    private final OfferUpdateRepository offerUpdateRepository;
    private final ShopOfferUpdateRepository shopOfferUpdateRepository;
    private final OfferRepository offerRepository;


    @Transactional()
    public List<OfferShopUpdateInfoDto> getOfferUpdates() {
        List<OfferUpdate> all = offerUpdateRepository.findAll();
        List<OfferShopUpdateInfoDto> result = new ArrayList<>();

        for (OfferUpdate offerUpdate : all) {
            OfferShopUpdateInfoDto dto = new OfferShopUpdateInfoDto();
            dto.setId(offerUpdate.getId());
            dto.setShops(new ArrayList<>());
            dto.setStartedAt(offerUpdate.getStartedAt());
            dto.setFinishedAt(offerUpdate.getFinishedAt());

            for (ShopOfferUpdate shopOfferUpdate : offerUpdate.getShopOfferUpdates()) {
                String shopName = shopOfferUpdate.getShop().getName();

                Map<String, Integer> offersAdded = countOffersByType(
                        shopOfferUpdate,
                        UpdateChangeType.ADDED
                );
                Map<String, Integer> offersDeleted = countOffersByType(
                        shopOfferUpdate,
                        UpdateChangeType.DELETED
                );

                OfferShopUpdateInfoDto.ShopUpdateInfoDto shopDto = new OfferShopUpdateInfoDto.ShopUpdateInfoDto();
                shopDto.setShopName(shopName);
                shopDto.setOffersAdded(offersAdded);
                shopDto.setOffersDeleted(offersDeleted);
                shopDto.setStatus(shopOfferUpdate.getStatus());

                dto.getShops().add(shopDto);
            }

            result.add(dto);
        }

        return result;
    }


    public OfferShopUpdateInfoDto.ShopUpdateInfoDto getShopUpdateInfo(String shopName, Long offerUpdateId) {

        ShopOfferUpdate shopOfferUpdate = shopOfferUpdateRepository
                .findFirstByOfferUpdate_IdAndShop_NameIgnoreCase(offerUpdateId, shopName)
                .orElseThrow(() -> new IllegalStateException("No ShopOfferUpdate for OfferUpdate.id=" + offerUpdateId + " and shop=" + shopName));

        OfferShopUpdateInfoDto.ShopUpdateInfoDto dto = new OfferShopUpdateInfoDto.ShopUpdateInfoDto();
        dto.setShopName(shopName);
        dto.setOffersAdded(Collections.emptyMap());
        dto.setOffersDeleted(Collections.emptyMap());
        dto.setStatus(shopOfferUpdate.getStatus());


        Map<String, Integer> addedByType = shopOfferUpdate.getOfferShopOfferUpdates().stream()
                .filter(link -> link.getUpdateChangeType() == UpdateChangeType.ADDED)
                .map(OfferShopOfferUpdate::getOffer)
                .filter(o -> o.getComponent() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getComponent().getComponentType().name(),
                        Collectors.summingInt(x -> 1)
                ));

        Map<String, Integer> deletedByType = shopOfferUpdate.getOfferShopOfferUpdates().stream()
                .filter(link -> link.getUpdateChangeType() == UpdateChangeType.DELETED)
                .map(OfferShopOfferUpdate::getOffer)
                .filter(o -> o.getComponent() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getComponent().getComponentType().name(),
                        Collectors.summingInt(x -> 1)
                ));

        dto.setOffersAdded(addedByType);
        dto.setOffersDeleted(deletedByType);
        return dto;
    }

    private Map<String, Integer> countOffersByType(
            ShopOfferUpdate shopOfferUpdate,
            UpdateChangeType changeType
    ) {
        return shopOfferUpdate.getOfferShopOfferUpdates().stream()
                .filter(link -> link.getUpdateChangeType() == changeType)
                .map(OfferShopOfferUpdate::getOffer)
                .filter(offer -> offer.getComponent() != null)
                .collect(Collectors.groupingBy(
                        offer -> offer.getComponent().getComponentType().name(),
                        Collectors.summingInt(o -> 1)
                ));
    }


    public List<OfferUpdateStatsDTO> getOfferStatsLast30Days() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return offerUpdateRepository.findOfferStatsSince(thirtyDaysAgo);
    }

    public List<OfferUpdateRepository.OfferUpdateShopsOffersAmountStatsProjection> getOffersShopsAmountStats() {
        return offerUpdateRepository.findOfferStatsByShop();
    }

}



