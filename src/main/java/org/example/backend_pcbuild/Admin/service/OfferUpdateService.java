package org.example.backend_pcbuild.Admin.service;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.OfferShopUpdateInfoDto;
import org.example.backend_pcbuild.models.Offer;
import org.example.backend_pcbuild.models.OfferShopOfferUpdate;
import org.example.backend_pcbuild.models.OfferUpdate;
import org.example.backend_pcbuild.models.ShopOfferUpdate;
import org.example.backend_pcbuild.Admin.repository.OfferUpdateRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferUpdateService {
    private final OfferUpdateRepository offerUpdateRepository;


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

                Map<String, Integer> totalOffers = new HashMap<>();
                Set<String> allTypes = new HashSet<>();
                allTypes.addAll(offersAdded.keySet());
                allTypes.addAll(offersDeleted.keySet());

                for (String type : allTypes) {
                    int added = offersAdded.getOrDefault(type, 0);
                    int deleted = offersDeleted.getOrDefault(type, 0);
                    int previous = 0;
                    totalOffers.put(type, previous + added - deleted);
                }

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
                        offer -> offer.getItem().getItemType().name(),
                        Collectors.summingInt(o -> 1)
                ));
    }


//    public OfferShopUpdateInfoDto.ShopUpdateInfoDto getShopUpdateInfo(String shopName, Long offerUpdateId, boolean isVisible) {
//        OfferUpdate update = offerUpdateRepository.findByIdAndShopName(offerUpdateId, shopName, isVisible).orElseThrow();
//
//        ShopOfferUpdate shopUpdate = update.getShopOfferUpdates().stream()
//                .filter(sou -> sou.getShop().getName().equalsIgnoreCase(shopName))
//                .findFirst()
//                .orElseThrow(() -> new IllegalStateException("No ShopOfferUpdate for shop " + shopName));
//
//        List<Offer> offers = shopUpdate.getOfferShopOfferUpdates().stream()
//                .map(OfferShopOfferUpdate::getOffer)
//                .filter(offer -> offer.getIsVisible() == isVisible)
//                .toList();
//
//        Map<String, Integer> offersByType = offers.stream()
//                .collect(Collectors.groupingBy(
//                        offer -> offer.getItem().getItemType().name(),
//                        Collectors.summingInt(o -> 1)
//                ));
//
//        OfferShopUpdateInfoDto.ShopUpdateInfoDto dto = new OfferShopUpdateInfoDto.ShopUpdateInfoDto();
//        dto.setShopName(shopName);
//
//        if (isVisible) {
//            dto.setOffersAdded(offersByType);
//        } else {
//            dto.setOffersDeleted(offersByType);
//        }
//        return dto;
//    }

    public OfferShopUpdateInfoDto.ShopUpdateInfoDto getShopUpdateInfo(String shopName, Long offerUpdateId, boolean isVisible) {
        Optional<OfferUpdate> optionalUpdate = offerUpdateRepository.findByIdAndShopName(offerUpdateId, shopName, isVisible);

        if (optionalUpdate.isEmpty()) {
            System.out.printf("Brak danych dla shop=%s, updateId=%d, visible=%b%n",
                    shopName, offerUpdateId, isVisible);

            OfferShopUpdateInfoDto.ShopUpdateInfoDto emptyDto = new OfferShopUpdateInfoDto.ShopUpdateInfoDto();
            emptyDto.setShopName(shopName);
            emptyDto.setOffersAdded(Collections.emptyMap());
            emptyDto.setOffersDeleted(Collections.emptyMap());
            emptyDto.setTotalOffers(Collections.emptyMap());
            return emptyDto;
        }

        OfferUpdate update = optionalUpdate.get();

        ShopOfferUpdate shopUpdate = update.getShopOfferUpdates().stream()
                .filter(sou -> sou.getShop().getName().equalsIgnoreCase(shopName))
                .findFirst()
                .orElse(null);

        if (shopUpdate == null) {
            OfferShopUpdateInfoDto.ShopUpdateInfoDto emptyDto = new OfferShopUpdateInfoDto.ShopUpdateInfoDto();
            emptyDto.setShopName(shopName);
            emptyDto.setOffersAdded(Collections.emptyMap());
            emptyDto.setOffersDeleted(Collections.emptyMap());
            emptyDto.setTotalOffers(Collections.emptyMap());
            return emptyDto;
        }

        List<Offer> offers = shopUpdate.getOfferShopOfferUpdates().stream()
                .map(OfferShopOfferUpdate::getOffer)
                .filter(offer -> offer.getIsVisible() == isVisible)
                .toList();

        Map<String, Integer> offersByType = offers.stream()
                .collect(Collectors.groupingBy(
                        offer -> offer.getItem().getItemType().name(),
                        Collectors.summingInt(o -> 1)
                ));

        OfferShopUpdateInfoDto.ShopUpdateInfoDto dto = new OfferShopUpdateInfoDto.ShopUpdateInfoDto();
        dto.setShopName(shopName);

        if (isVisible) {
            dto.setOffersAdded(offersByType);
        } else {
            dto.setOffersDeleted(offersByType);
        }

        return dto;
    }





}
