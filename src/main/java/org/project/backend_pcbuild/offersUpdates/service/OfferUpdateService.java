package org.project.backend_pcbuild.offersUpdates.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.backend_pcbuild.offer.model.Offer;
import org.project.backend_pcbuild.offer.model.Shop;
import org.project.backend_pcbuild.offer.repository.OfferRepository;
import org.project.backend_pcbuild.offer.repository.ShopRepository;
import org.project.backend_pcbuild.offersUpdates.dto.OfferShopUpdateInfoDto;
import org.project.backend_pcbuild.offersUpdates.dto.OfferUpdateStatsDTO;
import org.project.backend_pcbuild.offersUpdates.model.*;
import org.project.backend_pcbuild.offersUpdates.repository.OfferUpdateConfigRepository;
import org.project.backend_pcbuild.offersUpdates.repository.OfferUpdateRepository;
import org.project.backend_pcbuild.offersUpdates.repository.ShopOfferUpdateRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferUpdateService {
    private final OfferUpdateRepository offerUpdateRepository;
    private final ShopOfferUpdateRepository shopOfferUpdateRepository;
    private final OfferRepository offerRepository;
    private final RabbitTemplate rabbitTemplate;
    private final OfferUpdateConfigRepository offerUpdateConfigRepository;
    private final ShopRepository shopRepository;

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

    public boolean checkIfUpdateTypeIsCompleted(Long offerUpdateId, String shopName, UpdateChangeType type) {

       Optional <ShopOfferUpdate> shopOfferUpdate = shopOfferUpdateRepository.findFirstByOfferUpdate_IdAndShop_NameIgnoreCase(offerUpdateId, shopName);
         boolean isCompleted = false;
       if (shopOfferUpdate.isPresent()){
           long count = shopOfferUpdate.get().getOfferShopOfferUpdates().stream()
                   .filter(link -> link.getUpdateChangeType() == type)
                   .map(OfferShopOfferUpdate::getOffer)
                   .filter(offer -> offer.getComponent() != null)
                   .count();

           isCompleted = count > 0;
       }

       return isCompleted;

    }

    public Runnable scheduledAutomaticOfferUpdate() {
        return this::createAutomaticUpdate;
        };

    @Transactional
    public void createAutomaticUpdate() {
        OfferUpdate offerUpdate = new OfferUpdate();
        offerUpdate.setStartedAt(LocalDateTime.now());
        offerUpdate.setStatus(OfferUpdateStatus.RUNNING);

        OfferUpdateConfig config = offerUpdateConfigRepository.findByType(OfferUpdateType.AUTOMATIC)
                .orElseThrow();
        offerUpdate.setOfferUpdateConfig(config);


        List<Shop> allShops = shopRepository.findAll();
        System.out.println("lista sklepów: "+ allShops.size());
        List<String> urls = offerRepository.findAll().stream()
                .map(Offer::getWebsiteUrl)
                .toList();

        List<OfferShopUpdateInfoDto.ShopUpdateInfoDto> shopInfos = new ArrayList<>();
        List<Shop> shopsToProcess = new ArrayList<>();

        for (Shop shop : allShops) {
            boolean alreadyAdded = offerUpdate.getShopOfferUpdates().stream()
                    .anyMatch(sou -> sou.getShop().getName().equals(shop.getName()));
            System.out.println("porównanie: "+ shop.getName() + " "+alreadyAdded);

            if (alreadyAdded) {
                System.out.println("Pomijam sklep (już dodany): " + shop.getName());
                continue;
            }

            ShopOfferUpdate shopOfferUpdate = new ShopOfferUpdate();
            shopOfferUpdate.setOfferUpdate(offerUpdate);
            shopOfferUpdate.setShop(shop);
            shopOfferUpdate.setStatus(ShopUpdateStatus.RUNNING);

            offerUpdate.getShopOfferUpdates().add(shopOfferUpdate);

            shopsToProcess.add(shop);
            shopInfos.add(new OfferShopUpdateInfoDto.ShopUpdateInfoDto(
                    shop.getName(),
                    new HashMap<>(),
                    new HashMap<>(),
                    ShopUpdateStatus.RUNNING
            ));
        }

        OfferUpdate savedUpdate = offerUpdateRepository.save(offerUpdate);
        System.out.println(shopInfos);

        for (Shop shop : shopsToProcess) {
            try {
                Map<String, Object> checkingPayload = Map.of(
                        "updateId", savedUpdate.getId(),
                        "shop", shop.getName(),
                        "urls", urls
                );
                rabbitTemplate.convertAndSend("checkOffers." + shop.getName(), checkingPayload);

                Map<String, Object> scrapingPayload = Map.of(
                        "updateId", savedUpdate.getId(),
                        "shop", shop.getName()
                );
                rabbitTemplate.convertAndSend("scrapingOffers." + shop.getName(), scrapingPayload);

            } catch (Exception e) {
                log.error("Błąd przy wysyłaniu do RabbitMQ dla sklepu: {}", shop.getName(), e);
            }
        }
    }

    public OfferUpdateType getLastUpdateType() {
        Optional<OfferUpdate> firstByOrderByFinishedAtDesc = offerUpdateRepository.findFirstByOrderByFinishedAtDesc();
        return firstByOrderByFinishedAtDesc.map(offerUpdate -> offerUpdate.getOfferUpdateConfig().getType()).orElse(null);
    }
}



