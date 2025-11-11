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

                Map<String, Integer> offersAdded = countOffersByType(shopOfferUpdate, true);
                Map<String, Integer> offersDeleted = countOffersByType(shopOfferUpdate, false);

                Map<String, Integer> totalOffers = offerRepository.countVisibleOffersByShop(shopName)
                        .stream()
                        .collect(Collectors.toMap(
                                OfferRepository.OfferTypeCountProjection::getType,
                                p -> p.getCount().intValue()
                        ));


                OfferShopUpdateInfoDto.ShopUpdateInfoDto shopDto = new OfferShopUpdateInfoDto.ShopUpdateInfoDto();
                shopDto.setShopName(shopName);
                shopDto.setOffersAdded(offersAdded);
                shopDto.setOffersDeleted(offersDeleted);
                shopDto.setTotalOffers(totalOffers);

                dto.getShops().add(shopDto);
            }

            result.add(dto);
        }

        return result;
    }


    private Map<String, Integer> countOffersByType(ShopOfferUpdate shopOfferUpdate, boolean isVisible) {
        return shopOfferUpdate.getOfferShopOfferUpdates().stream()
                .map(OfferShopOfferUpdate::getOffer)
                .filter(offer -> offer.getIsVisible() == isVisible)
                .collect(Collectors.groupingBy(
                        offer -> offer.getItem().getComponentType().name(),
                        Collectors.summingInt(o -> 1)
                ));
    }

    public OfferShopUpdateInfoDto.ShopUpdateInfoDto getShopUpdateInfo(String shopName, Long offerUpdateId, boolean isVisible) {

        ShopOfferUpdate shopOfferUpdate = shopOfferUpdateRepository
                .findFirstByOfferUpdate_IdAndShop_NameIgnoreCase(offerUpdateId, shopName)
                .orElseThrow(() -> new IllegalStateException("No ShopOfferUpdate for OfferUpdate.id=" + offerUpdateId + " and shop=" + shopName));

        OfferShopUpdateInfoDto.ShopUpdateInfoDto dto = new OfferShopUpdateInfoDto.ShopUpdateInfoDto();
        dto.setShopName(shopName);
        dto.setTotalOffers(Collections.emptyMap());
        dto.setOffersAdded(Collections.emptyMap());
        dto.setOffersDeleted(Collections.emptyMap());

        if (shopOfferUpdate == null) {
            return dto;
        }

        Map<String, Integer> totalByType = offerRepository
                .countVisibleOffersByShop(shopName)
                .stream()
                .collect(Collectors.toMap(
                        OfferRepository.OfferTypeCountProjection::getType,
                        p -> p.getCount().intValue()
                ));

        List<Offer> offersFromUpdate = shopOfferUpdate.getOfferShopOfferUpdates().stream()
                .map(OfferShopOfferUpdate::getOffer)
                .filter(o -> o.getItem() != null)
                .toList();

        Map<String, Integer> addedByType = offersFromUpdate.stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsVisible()))
                .collect(Collectors.groupingBy(
                        o -> o.getItem().getComponentType().name(),
                        Collectors.summingInt(x -> 1)
                ));

        Map<String, Integer> deletedByType = offersFromUpdate.stream()
                .filter(o -> Boolean.FALSE.equals(o.getIsVisible()))
                .collect(Collectors.groupingBy(
                        o -> o.getItem().getComponentType().name(),
                        Collectors.summingInt(x -> 1)
                ));

        dto.setTotalOffers(totalByType);
        dto.setOffersAdded(addedByType);
        dto.setOffersDeleted(deletedByType);
        return dto;
    }

//    public List<OfferUpdateStatsDTO> getOfferUpdateStats() {
//        return offerUpdateRepository.findOfferStatsLast30Days();
//    }

//    public List<OfferUpdateRepository.OfferUpdateStatsProjection> getOfferUpdateStats() {
//        return offerUpdateRepository.findOfferStatsLast30Days();
//    }
//public List<OfferUpdateStatsDTO> getOfferStats() {
//    return repo.findOfferStatsLast30DaysNative().stream()
//            .map(r -> new OfferUpdateStatsDTO(((Number) r[0]).longValue(), ((Date) r[1]).toLocalDate()))
//            .toList();
//}

    public List<OfferUpdateStatsDTO> getOfferStatsLast30Days() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return offerUpdateRepository.findOfferStatsSince(thirtyDaysAgo);
    }

    public List<OfferUpdateRepository.OfferUpdateShopsOffersAmountStatsProjection> getOffersShopsAmountStats() {
        return offerUpdateRepository.findOfferStatsByShop();
    }

}



