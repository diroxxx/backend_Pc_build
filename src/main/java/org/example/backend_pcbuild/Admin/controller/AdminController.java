package org.example.backend_pcbuild.Admin.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.*;
import org.example.backend_pcbuild.Admin.repository.OfferUpdateConfigRepository;
import org.example.backend_pcbuild.Admin.service.AdminService;
import org.example.backend_pcbuild.Admin.service.OfferUpdateConfigService;
import org.example.backend_pcbuild.Admin.service.OfferUpdateService;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserMapper;
import org.example.backend_pcbuild.Component.service.ComponentService;
import org.example.backend_pcbuild.Offer.service.OfferService;
import org.example.backend_pcbuild.models.*;
import org.example.backend_pcbuild.repository.OfferRepository;
import org.example.backend_pcbuild.Admin.repository.OfferUpdateRepository;
import org.example.backend_pcbuild.Admin.repository.ShopOfferUpdateRepository;
import org.example.backend_pcbuild.repository.ShopRepository;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OfferService offerService;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    private final OfferUpdateConfigService offerUpdateConfigService;

    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;

    private final UserMapper userMapper;

    private final OfferRepository offerRepository;

    private final ShopRepository shopRepository;

    private final OfferUpdateRepository offerUpdateRepository;
    private final OfferUpdateConfigRepository offerUpdateConfigRepository;
    private final ShopOfferUpdateRepository shopOfferUpdateRepository;
    private final OfferUpdateService offerUpdateService;
    private final ComponentService componentService;
    private final OfferUpdateConfigService configService;
    private final AdminService adminService;


    @RabbitListener(queues = {
            "test1"
    })
    @Transactional
    public void test(Message amqpMessage) {
        String json = new String(amqpMessage.getBody(), StandardCharsets.UTF_8);
        System.out.println(json);
    }


    //TEST
    @GetMapping("/test")
    public ResponseEntity<List<OfferShopUpdateInfoDto>> getUpdateInfo(){
        List<OfferShopUpdateInfoDto> offerUpdates = offerUpdateService.getOfferUpdates();
        return ResponseEntity.ok(offerUpdates);
    }

    //        @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(value = "/components",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Object>>> getComponents() {
        Map<String, List<Object>> result = componentService.fetchComponentsAsMap();
        componentService.saveBasedComponents(result);
        return ResponseEntity.ok(result);
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
        // Sprawdzamy oba typy
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
            "offersAdded.x-kom",
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

            // zaktualizuj status sklepu patrząc na oba typy
            updateShopStatusBasedOnTypes(shopOfferUpdate, offerUpdateId, shopName);
            shopOfferUpdateRepository.save(shopOfferUpdate);

            // zaktualizuj globalny status
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

            // zaktualizuj status sklepu patrząc na oba typy
            updateShopStatusBasedOnTypes(shopOfferUpdate, offerUpdateId, shopName);
            shopOfferUpdateRepository.save(shopOfferUpdate);

            // zaktualizuj globalny status
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









//    @RabbitListener(queues = {
//            "offersAdded.olx",
//            "offersAdded.allegro",
//            "offersAdded.x-kom",
//            "offersAdded.allegroLokalnie"
//    })
//    @Transactional
//    public void handleOffersAdded(Message amqpMessage) {
//        OfferUpdate offerUpdate = null;
//        ShopOfferUpdate shopOfferUpdate = null;
//
//        try {
//            String json = new String(amqpMessage.getBody(), StandardCharsets.UTF_8);
//            ScrapingOfferDto dto = objectMapper.readValue(json, ScrapingOfferDto.class);
//
//            if (dto == null) {
//                throw new IllegalArgumentException("ScrapingOfferDto is null");
//            }
//
//            Long offerUpdateId = dto.getUpdateId();
//            String shopName = dto.getShopName();
//
//            if (offerUpdateId == null || shopName == null || shopName.isBlank()) {
//                throw new IllegalArgumentException("Invalid payload: updateId=" + offerUpdateId +
//                        ", shopName=" + shopName);
//            }
//
//            shopOfferUpdate = shopOfferUpdateRepository
//                    .findFirstByOfferUpdate_IdAndShop_NameIgnoreCase(offerUpdateId, shopName)
//                    .orElseThrow(() -> new IllegalStateException(
//                            "No ShopOfferUpdate for OfferUpdate.id=" + offerUpdateId + " and shop=" + shopName));
//
//            List<ComponentOfferDto> components = dto.getComponentsData();
//            if (components == null) {
//                components = new ArrayList<>();
//            }
//            System.out.println("zapisywanie dla sklepu " + shopName + ", ilsoc: " + components.size());
//
//            offerService.saveOffersTemplate(components, shopOfferUpdate);
//
//            offerUpdate = shopOfferUpdate.getOfferUpdate();
//
//            boolean anyFailed = offerUpdate.getShopOfferUpdates().stream()
//                    .anyMatch(su -> su.getStatus() == ShopUpdateStatus.FAILED);
//
//            boolean allDone = offerUpdate.getShopOfferUpdates().stream()
//                    .allMatch(su -> su.getStatus() == ShopUpdateStatus.COMPLETED
//                            || su.getStatus() == ShopUpdateStatus.FAILED);
//
//            if (allDone) {
//                offerUpdate.setStatus(anyFailed ? OfferUpdateStatus.FAILED : OfferUpdateStatus.COMPLETED);
//                offerUpdate.setFinishedAt(LocalDateTime.now());
//            }
//            if (shopOfferUpdate.getStatus() == ShopUpdateStatus.PARTIAL_COMPLETED || shopOfferUpdate.getStatus() == ShopUpdateStatus.COMPLETED) {
//                offerUpdate.setFinishedAt(LocalDateTime.now());
//                shopOfferUpdate.setStatus(ShopUpdateStatus.COMPLETED);
//            } else if (shopOfferUpdate.getStatus() != ShopUpdateStatus.FAILED) {
//                shopOfferUpdate.setStatus(ShopUpdateStatus.PARTIAL_COMPLETED);
//            }
//
//            boolean isDeleted = offerUpdateService.checkIfUpdateTypeIsCompleted(offerUpdateId, shopName, UpdateChangeType.DELETED);
//            System.out.println("isDeleted: " + isDeleted);
//            shopOfferUpdate.setStatus(isDeleted ? ShopUpdateStatus.COMPLETED : ShopUpdateStatus.RUNNING);
//
//            shopOfferUpdateRepository.save(shopOfferUpdate);
//
//            offerUpdateRepository.save(offerUpdate);
//
//            OfferShopUpdateInfoDto.ShopUpdateInfoDto shopUpdateInfo =
//                    offerUpdateService.getShopUpdateInfo(shopName, offerUpdateId);
//
//
//            boolean offerUpdateFinished = offerUpdateService.isOfferUpdateFinished(offerUpdateId);
//            if (offerUpdateFinished) {
//                Optional<OfferUpdate> byId = offerUpdateRepository.findById(offerUpdateId);
//                if (byId.isPresent()){
//                    byId.get().setFinishedAt(LocalDateTime.now());
//                    offerUpdateRepository.save(byId.get());
//                }
//            }
//
//            messagingTemplate.convertAndSend("/topic/offers/" + offerUpdateId, shopUpdateInfo);
//
//        } catch (Exception e) {
//            if (offerUpdate != null) {
//                offerUpdate.setStatus(OfferUpdateStatus.FAILED);
//                offerUpdate.setFinishedAt(LocalDateTime.now());
//                offerUpdateRepository.save(offerUpdate);
//            }
//            if (shopOfferUpdate != null) {
//                shopOfferUpdate.setStatus(ShopUpdateStatus.FAILED);
//                shopOfferUpdateRepository.save(shopOfferUpdate);
//            }
//            throw new AmqpRejectAndDontRequeueException(e);
//        }
//    }
//
//    @RabbitListener(queues = {
//            "offersDeleted.olx",
//            "offersDeleted.allegro",
//            "offersDeleted.x-kom",
//            "offersDeleted.allegroLokalnie"
//    })
//    @Transactional
//    public void handleOffersDeleted(Message amqpMessage) {
//
//        OfferUpdate offerUpdate = null;
//        try {
//            String json = new String(amqpMessage.getBody(), StandardCharsets.UTF_8);
//            JsonNode node = objectMapper.readTree(json);
//
//            if (!node.has("updateId") || !node.has("shop")) {
//                System.out.println("Brak updateId lub shop w wiadomości: " + json);
//                return;
//            }
//
//            Long offerUpdateId = node.get("updateId").asLong();
//            String shopName = node.get("shop").asText();
//
//            System.out.println("Deleting shop " + shopName);
//
//            List<String> urls = new ArrayList<>();
//            if (node.has("urls") && node.get("urls").isArray()) {
//                urls = objectMapper.convertValue(node.get("urls"), new TypeReference<List<String>>() {});
//            }
//
//            ShopOfferUpdate shopOfferUpdate = shopOfferUpdateRepository
//                    .findFirstByOfferUpdate_IdAndShop_NameIgnoreCase(offerUpdateId, shopName)
//                    .orElseThrow(() -> new IllegalStateException("No ShopOfferUpdate for OfferUpdate.id=" + offerUpdateId + " and shop=" + shopName));
//
//            offerService.softDeleteByUrls(urls, shopOfferUpdate);
//
//
//            boolean isAdded = offerUpdateService.checkIfUpdateTypeIsCompleted(offerUpdateId, shopName, UpdateChangeType.ADDED);
//            shopOfferUpdate.setStatus(isAdded ? ShopUpdateStatus.COMPLETED : ShopUpdateStatus.RUNNING);
//
//            shopOfferUpdateRepository.save(shopOfferUpdate);
//
//
//            OfferShopUpdateInfoDto.ShopUpdateInfoDto shopUpdateInfo =
//                    offerUpdateService.getShopUpdateInfo(shopName, offerUpdateId);
//
//            boolean offerUpdateFinished = offerUpdateService.isOfferUpdateFinished(offerUpdateId);
//
//            offerUpdate =  shopOfferUpdate.getOfferUpdate();
//
//            if (shopOfferUpdate.getStatus() == ShopUpdateStatus.PARTIAL_COMPLETED || shopOfferUpdate.getStatus() == ShopUpdateStatus.COMPLETED) {
//                offerUpdate.setFinishedAt(LocalDateTime.now());
//                shopOfferUpdate.setStatus(ShopUpdateStatus.COMPLETED);
//            } else if (shopOfferUpdate.getStatus() != ShopUpdateStatus.FAILED) {
//                shopOfferUpdate.setStatus(ShopUpdateStatus.PARTIAL_COMPLETED);
//            }
//
//            if (offerUpdateFinished) {
//                Optional<OfferUpdate> byId = offerUpdateRepository.findById(offerUpdateId);
//                if (byId.isPresent()){
//                    byId.get().setFinishedAt(LocalDateTime.now());
//                    offerUpdateRepository.save(byId.get());
//                }
//            }
//
//            System.out.println("Usunięto oferty dla sklepu " + shopName);
//            messagingTemplate.convertAndSend("/topic/offers/" + offerUpdateId, shopUpdateInfo);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new AmqpRejectAndDontRequeueException(e);
//        }
//    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/OfferUpdateConfig")
    public ResponseEntity<?> updateOfferConfig(@RequestBody OfferUpdateConfigDto offerUpdateConfigDto) {

        if (offerUpdateConfigDto.getType() == OfferUpdateType.AUTOMATIC
                && offerUpdateConfigDto.getIntervalInMinutes() == null) {

            return ResponseEntity.badRequest().body(Map.of("message", "Automatic update interval cannot be null"));
        }
        return ResponseEntity.ok(Map.of("message", "Offer update config updated"));
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/users")
    public ResponseEntity<?> deleteUserByEmail(@RequestParam("email") String email) {
        boolean existed = userRepository.existsByEmail(email);
        if (!existed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Użytkownik nie znaleziony"));
        }
        adminService.deleteUserByEmail(email);
        return ResponseEntity.ok(Map.of("message", "Użytkownik został usunięty"));
    }
}



