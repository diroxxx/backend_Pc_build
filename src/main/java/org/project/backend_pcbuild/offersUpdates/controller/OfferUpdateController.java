package org.project.backend_pcbuild.offersUpdates.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.project.backend_pcbuild.offer.model.Offer;
import org.project.backend_pcbuild.offer.model.Shop;
import org.project.backend_pcbuild.offersUpdates.model.*;
import org.project.backend_pcbuild.offersUpdates.dto.*;
import org.project.backend_pcbuild.offersUpdates.model.*;
import org.project.backend_pcbuild.offersUpdates.repository.OfferUpdateConfigRepository;
import org.project.backend_pcbuild.offersUpdates.service.OfferUpdateConfigService;
import org.project.backend_pcbuild.offersUpdates.service.OfferUpdateService;
import org.project.backend_pcbuild.offer.dto.ComponentOfferDto;
import org.project.backend_pcbuild.offer.service.OfferService;
import org.project.backend_pcbuild.offersUpdates.dto.*;
import org.project.backend_pcbuild.offer.repository.OfferRepository;
import org.project.backend_pcbuild.offersUpdates.repository.OfferUpdateRepository;
import org.project.backend_pcbuild.offersUpdates.repository.ShopOfferUpdateRepository;
import org.project.backend_pcbuild.offer.repository.ShopRepository;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin("http://127.0.0.1:5000")
@RequestMapping("/api/offersUpdates")
@RequiredArgsConstructor
public class OfferUpdateController {

    private final OfferService offerService;

    private final ObjectMapper objectMapper;

    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;

    private final OfferRepository offerRepository;

    private final ShopRepository shopRepository;

    private final OfferUpdateRepository offerUpdateRepository;
    private final OfferUpdateConfigRepository offerUpdateConfigRepository;
    private final ShopOfferUpdateRepository shopOfferUpdateRepository;
    private final OfferUpdateService offerUpdateService;
    private final OfferUpdateConfigService configService;


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping()
    public ResponseEntity<List<OfferShopUpdateInfoDto>> getOffersUpdates() {

        return ResponseEntity.ok(offerUpdateService.getOfferUpdates());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<List<OfferUpdateStatsDTO>> getOfferUpdateStats() {
        return ResponseEntity.ok(offerUpdateService.getOfferStatsLast30Days());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("stats/shops")
    public ResponseEntity<List<OfferUpdateRepository.OfferUpdateShopsOffersAmountStatsProjection>> getOffersShopsAmountStats() {
        return ResponseEntity.ok(offerUpdateService.getOffersShopsAmountStats());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/lastUpdateType")
    public ResponseEntity<?> getLastUpdateType() {
        OfferUpdateType lastUpdateType = offerUpdateService.getLastUpdateType();

        return ResponseEntity.ok(lastUpdateType);
    }



    @PutMapping("/update/automatic")
    public void saveRequestToAutomaticUpdate(@RequestParam("interval") String interval) {
        configService.updateUpdateInterval(interval);
    }

    @MessageMapping("/offers")
    @SendTo("/topic/offers")
    @Transactional
    public void sendRequestToManualUpdate(@Payload ManualFetchOffersSettingsDto settings) {
        System.out.println(settings.getShops());
        OfferUpdate offerUpdate = new OfferUpdate();
        offerUpdate.setStartedAt(LocalDateTime.now());
        offerUpdate.setStatus(OfferUpdateStatus.RUNNING);
        OfferUpdate saveUpdate = offerUpdateRepository.save(offerUpdate);

                OfferUpdateConfig offerUpdateConfig = offerUpdateConfigRepository.findByType(OfferUpdateType.MANUAL)
                .orElseThrow();
        offerUpdate.setOfferUpdateConfig(offerUpdateConfig);

        List<OfferShopUpdateInfoDto.ShopUpdateInfoDto> shopInfos = new ArrayList<>();

        for (String shop : settings.getShops()) {
            Shop shopForAdd = shopRepository.findByNameIgnoreCase(shop).orElseThrow();
            System.out.println("znalazłem sklep " + shop);

            boolean alreadyAdded = offerUpdate.getShopOfferUpdates().stream()
                    .anyMatch(u -> u.getShop().getId().equals(shopForAdd.getId()));
            if (alreadyAdded) {
                System.out.println("Duplicate ShopOfferUpdate detected for shop=" + shop + ", skipping.");
                continue;
            }

            ShopOfferUpdate shopOfferUpdate = new ShopOfferUpdate();
            shopOfferUpdate.setOfferUpdate(offerUpdate);
            shopOfferUpdate.setShop(shopForAdd);
            shopOfferUpdate.setStatus(ShopUpdateStatus.RUNNING);

            offerUpdate.getShopOfferUpdates().add(shopOfferUpdate);

            shopInfos.add(new OfferShopUpdateInfoDto.ShopUpdateInfoDto(
                    shop,
                    new HashMap<>(),
                    new HashMap<>(),
                    ShopUpdateStatus.RUNNING
            ));
            offerUpdateRepository.save(offerUpdate);
        }

        OfferShopUpdateInfoDto dto = new OfferShopUpdateInfoDto();
        dto.setId(saveUpdate.getId());
        dto.setShops(new ArrayList<>());
        dto.setStartedAt(saveUpdate.getStartedAt());
        dto.getShops().addAll(shopInfos);

        messagingTemplate.convertAndSend("/topic/offers", dto);

        for (String shop : settings.getShops()) {
            List<String> listOfUrls = offerRepository
                    .findAll()
                    .stream()
                    .filter(offer -> offer.getShop().getName().equalsIgnoreCase(shop))
                    .filter(Offer::getIsVisible)
                    .map(Offer::getWebsiteUrl)
                    .toList();
            try {
                String urlsJson = new ObjectMapper().writeValueAsString(listOfUrls);
                if (urlsJson == null) {
                    continue;
                }

                Map<String, Object> checkingPayload = Map.of(
                        "updateId", saveUpdate.getId(),
                        "shop", shop,
                        "urls", listOfUrls
                );
                rabbitTemplate.convertAndSend("checkOffers." + shop, checkingPayload);

                Map<String, Object> scrapingPayload = Map.of(
                        "updateId", saveUpdate.getId(),
                        "shop", shop
                );
                rabbitTemplate.convertAndSend("scrapingOffers." + shop, scrapingPayload);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private void recomputeAndSaveGlobalOfferUpdate(OfferUpdate offerUpdate) {
        long total = offerUpdate.getShopOfferUpdates().size();
        long completed = offerUpdate.getShopOfferUpdates().stream()
                .filter(su -> su.getStatus() == ShopUpdateStatus.COMPLETED).count();
        long failed = offerUpdate.getShopOfferUpdates().stream()
                .filter(su -> su.getStatus() == ShopUpdateStatus.FAILED).count();
        long running = offerUpdate.getShopOfferUpdates().stream()
                .filter(su -> su.getStatus() == ShopUpdateStatus.RUNNING).count();

        if (running > 0) {
            offerUpdate.setStatus(OfferUpdateStatus.RUNNING);
        } else {
            if (failed == total) {
                offerUpdate.setStatus(OfferUpdateStatus.FAILED);
            } else if (completed == total) {
                offerUpdate.setStatus(OfferUpdateStatus.COMPLETED);
            } else {
                offerUpdate.setStatus(OfferUpdateStatus.RUNNING);
            }
            if (offerUpdate.getFinishedAt() == null) {
                offerUpdate.setFinishedAt(LocalDateTime.now());
            }
        }
        offerUpdateRepository.save(offerUpdate);
    }

    private void updateShopStatusBasedOnTypes(ShopOfferUpdate shop, Long offerUpdateId, String shopName) {
        boolean addedCompleted = offerUpdateService.checkIfUpdateTypeIsCompleted(offerUpdateId, shopName, UpdateChangeType.ADDED);
        boolean deletedCompleted = offerUpdateService.checkIfUpdateTypeIsCompleted(offerUpdateId, shopName, UpdateChangeType.DELETED);

        if (shop.getStatus() == ShopUpdateStatus.FAILED) {

        } else if (addedCompleted && deletedCompleted) {
            shop.setStatus(ShopUpdateStatus.COMPLETED);
        } else if (addedCompleted || deletedCompleted) {
            shop.setStatus(ShopUpdateStatus.PARTIAL_COMPLETED);
        } else {
            shop.setStatus(ShopUpdateStatus.RUNNING);
        }
    }

    @RabbitListener(queues = {
            "offersAdded.olx",
            "offersAdded.allegro",
            "offersAdded.allegroLokalnie"
    })
    @Transactional
    public void handleOffersAdded(Message amqpMessage) {
        OfferUpdate offerUpdate = null;
        ShopOfferUpdate shopOfferUpdate = null;

        try {
            String json = new String(amqpMessage.getBody(), StandardCharsets.UTF_8);
            ScrapingOfferDto dto = objectMapper.readValue(json, ScrapingOfferDto.class);

            Long offerUpdateId = dto.getUpdateId();
            String shopName = dto.getShopName();

            shopOfferUpdate = shopOfferUpdateRepository
                    .findFirstByOfferUpdate_IdAndShop_NameIgnoreCase(offerUpdateId, shopName)
                    .orElseThrow(() -> new IllegalStateException("No ShopOfferUpdate for OfferUpdate.id=" + offerUpdateId + " and shop=" + shopName));

            List<ComponentOfferDto> components = dto.getComponentsData();
            if (components == null) components = new ArrayList<>();

            offerService.saveOffersTemplate(components, shopOfferUpdate);

            updateShopStatusBasedOnTypes(shopOfferUpdate, offerUpdateId, shopName);
            shopOfferUpdateRepository.save(shopOfferUpdate);

            offerUpdate = shopOfferUpdate.getOfferUpdate();
            recomputeAndSaveGlobalOfferUpdate(offerUpdate);

            OfferShopUpdateInfoDto.ShopUpdateInfoDto shopUpdateInfo =
                    offerUpdateService.getShopUpdateInfo(shopName, offerUpdateId);

            messagingTemplate.convertAndSend("/topic/offers/" + offerUpdateId, shopUpdateInfo);

        } catch (Exception e) {
            if (offerUpdate != null) {
                offerUpdate.setStatus(OfferUpdateStatus.FAILED);
                offerUpdate.setFinishedAt(LocalDateTime.now());
                offerUpdateRepository.save(offerUpdate);
            }
            if (shopOfferUpdate != null) {
                shopOfferUpdate.setStatus(ShopUpdateStatus.FAILED);
                shopOfferUpdateRepository.save(shopOfferUpdate);
            }
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @RabbitListener(queues = {
            "offersDeleted.olx",
            "offersDeleted.allegro",
            "offersDeleted.x-kom",
            "offersDeleted.allegroLokalnie"
    })
    @Transactional
    public void handleOffersDeleted(Message amqpMessage) {
        OfferUpdate offerUpdate = null;
        ShopOfferUpdate shopOfferUpdate = null;

        try {
            String json = new String(amqpMessage.getBody(), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(json);

            Long offerUpdateId = node.get("updateId").asLong();
            String shopName = node.get("shop").asText();

            List<String> urls = new ArrayList<>();
            if (node.has("urls") && node.get("urls").isArray()) {
                urls = objectMapper.convertValue(node.get("urls"), new TypeReference<List<String>>() {});
            }

            shopOfferUpdate = shopOfferUpdateRepository
                    .findFirstByOfferUpdate_IdAndShop_NameIgnoreCase(offerUpdateId, shopName)
                    .orElseThrow(() -> new IllegalStateException("No ShopOfferUpdate for OfferUpdate.id=" + offerUpdateId + " and shop=" + shopName));

            offerService.softDeleteByUrls(urls, shopOfferUpdate);

            updateShopStatusBasedOnTypes(shopOfferUpdate, offerUpdateId, shopName);
            shopOfferUpdateRepository.save(shopOfferUpdate);

            offerUpdate = shopOfferUpdate.getOfferUpdate();
            recomputeAndSaveGlobalOfferUpdate(offerUpdate);

            OfferShopUpdateInfoDto.ShopUpdateInfoDto shopUpdateInfo =
                    offerUpdateService.getShopUpdateInfo(shopName, offerUpdateId);

            messagingTemplate.convertAndSend("/topic/offers/" + offerUpdateId, shopUpdateInfo);

        } catch (Exception e) {
            e.printStackTrace();
            if (shopOfferUpdate != null) {
                shopOfferUpdate.setStatus(ShopUpdateStatus.FAILED);
                shopOfferUpdateRepository.save(shopOfferUpdate);
            }
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/OfferUpdateConfig")
    public ResponseEntity<?> updateOfferConfig(@RequestBody OfferUpdateConfigDto offerUpdateConfigDto) {

        if (offerUpdateConfigDto.getType() == OfferUpdateType.AUTOMATIC
                && offerUpdateConfigDto.getIntervalInMinutes() == null) {

            return ResponseEntity.badRequest().body(Map.of("message", "Automatic update interval cannot be null"));
        }
        return ResponseEntity.ok(Map.of("message", "Offer update config updated"));
    }
}



