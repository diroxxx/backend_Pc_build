package org.example.backend_pcbuild.Admin.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.security.auth.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.*;
import org.example.backend_pcbuild.Admin.repository.OfferUpdateConfigRepository;
import org.example.backend_pcbuild.Admin.service.OfferUpdateConfigService;
import org.example.backend_pcbuild.Admin.service.OfferUpdateService;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserMapper;
import org.example.backend_pcbuild.Component.ComponentService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

//    private final OfferUpdateBufferService bufferService;
    private final ShopRepository shopRepository;


    private final OfferUpdateRepository offerUpdateRepository;
    private final OfferUpdateConfigRepository offerUpdateConfigRepository;
    private final ShopOfferUpdateRepository shopOfferUpdateRepository;
    private final OfferUpdateService offerUpdateService;
    private final ComponentService componentService;


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


    @MessageMapping("/offers")
    @SendTo("/topic/offers")
    @Transactional
//    @PreAuthorize("hasAuthority('ADMIN')")
    public void sendRequestToScraper(@Payload ManualFetchOffersSettingsDto settings) {
        System.out.println(settings.getShops());
        //create OffersUpdate empty structure
        OfferUpdate offerUpdate = new OfferUpdate();
        offerUpdate.setStartedAt(LocalDateTime.now());
        OfferUpdate offerUpdateCreated = offerUpdateRepository.save(offerUpdate);

        List<OfferShopUpdateInfoDto.ShopUpdateInfoDto> shopInfos = new ArrayList<>();

        settings.getShops().forEach(shopName -> {
            shopInfos.add(new OfferShopUpdateInfoDto.ShopUpdateInfoDto(
                    shopName,
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>()
            ));
        });

        OfferShopUpdateInfoDto dto = new OfferShopUpdateInfoDto();
        dto.setId(offerUpdate.getId());
        dto.setShops(new ArrayList<>());
        dto.setStartedAt(offerUpdate.getStartedAt());
        dto.getShops().addAll(shopInfos);

        messagingTemplate.convertAndSend("/topic/offers", dto);
        for (String shop : settings.getShops()){

            Shop shopForAdd = shopRepository.findByNameIgnoreCase(shop).orElseThrow();

            if (shopOfferUpdateRepository.existsByOfferUpdate_IdAndShop_NameIgnoreCase(offerUpdateCreated.getId(), shop)) {
                System.out.println("Duplicate ShopOfferUpdate detected for shop=" + shop + ", skipping.");
                continue;
            }

                ShopOfferUpdate shopOfferUpdate = new ShopOfferUpdate();
            shopOfferUpdate.setOfferUpdate(offerUpdateCreated);
            shopOfferUpdate.setShop(shopForAdd);
            ShopOfferUpdate save = shopOfferUpdateRepository.save(shopOfferUpdate);


            OfferUpdateConfig offerUpdateConfig = offerUpdateConfigRepository.findByType(OfferUpdateType.MANUAL).orElseThrow();
            offerUpdate.setOfferUpdateConfig(offerUpdateConfig);
            offerUpdateConfig.getOfferUpdates().add(offerUpdateCreated);

            offerUpdateRepository.save(offerUpdate);
            shopRepository.save(shopForAdd);

                List<String> listOfUrls = offerRepository
                        .findAll()
                        .stream()
                        .map(Offer::getWebsiteUrl)
                        .toList();
                try{
                    String urlsJson = new ObjectMapper().writeValueAsString(listOfUrls);
                    if (urlsJson == null){
                        continue;
                    }

                    Map<String, Object> checkingPayload = Map.of(
//                            "updateId", (save.getId()),
                            "updateId", (offerUpdate.getId()),
                            "shop", shop,
                            "urls", listOfUrls
                    );
                    rabbitTemplate.convertAndSend("checkOffers." + shop, checkingPayload);
//                    rabbitTemplate.convertAndSend("checkOffers.olx", checkingPayload);

                    Map<String, Object> scrapingPayload = Map.of(
//                            "updateId", (save.getId()),
                            "updateId", (offerUpdate.getId()),
                            "shop", shop
                    );
                    rabbitTemplate.convertAndSend("scrapingOffers." + shop, scrapingPayload);

                }catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    private Map<String, Object> convertOfferDtoToMap(ComponentOfferDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("brand", dto.getBrand());
        map.put("category", dto.getCategory());
        map.put("img", dto.getImg());
        map.put("model", dto.getModel());
        map.put("price", dto.getPrice());
        map.put("shop", dto.getShop());
        map.put("status", dto.getStatus());
        map.put("url", dto.getUrl());
        return map;
    }

@RabbitListener(queues = {
        "offersAdded.olx",
        "offersAdded.allegro",
        "offersAdded.allegroLokalnie"
})@Transactional
public void handleOffersAdded(Message amqpMessage) {
    try {
        String json = new String(amqpMessage.getBody(), StandardCharsets.UTF_8);
        ScrapingOfferDto dto = objectMapper.readValue(json, ScrapingOfferDto.class);

        Long offerUpdateId = dto.getUpdateId();
        String shopName = dto.getShopName();
        System.out.println("adding shop offer: " + shopName);

        OfferUpdate offerUpdate = offerUpdateRepository.findById(offerUpdateId)
                .orElseThrow(() -> new IllegalStateException("No OfferUpdate for id=" + offerUpdateId));

        ShopOfferUpdate shopOfferUpdate = shopOfferUpdateRepository
                .findFirstByOfferUpdate_IdAndShop_NameIgnoreCase(offerUpdateId, shopName)
                .orElseThrow(() -> new IllegalStateException("No ShopOfferUpdate for OfferUpdate.id=" + offerUpdateId + " and shop=" + shopName));

        Map<String, List<Object>> offersByCategory = new HashMap<>();

        if (dto.getComponentsData() != null) {
            for (ComponentOfferDto offer : dto.getComponentsData()) {
                String category = offer.getCategory();
                if (category == null) continue;

                Map<String, Object> offerMap = convertOfferDtoToMap(offer);

                offersByCategory
                        .computeIfAbsent(category, k -> new ArrayList<>())
                        .add(offerMap);
            }
        }

        offerService.saveAllOffers(offersByCategory, shopOfferUpdate);

        if (offerUpdate.getStartedAt() == null) {
            offerUpdate.setStartedAt(LocalDateTime.now());
        }
        offerUpdate.setFinishedAt(LocalDateTime.now());
        offerUpdateRepository.save(offerUpdate);

        OfferShopUpdateInfoDto.ShopUpdateInfoDto shopUpdateInfo =
                offerUpdateService.getShopUpdateInfo(shopName, offerUpdateId, true);

        System.out.println("Dodano oferty dla sklepu " + shopName);
        messagingTemplate.convertAndSend("/topic/offers/" + offerUpdateId, shopUpdateInfo);

    } catch (Exception e) {
        e.printStackTrace();
        throw new AmqpRejectAndDontRequeueException(e);
    }
}

    @RabbitListener(queues = {
            "offersDeleted.olx",
            "offersDeleted.allegro",
            "offersDeleted.allegroLokalnie"
    })    @Transactional
    public void handleOffersDeleted(Message amqpMessage) {
        try {
            String json = new String(amqpMessage.getBody(), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(json);

            if (!node.has("updateId") || !node.has("shop")) {
                System.out.println("Brak updateId lub shop w wiadomości: " + json);
                return;
            }

            Long offerUpdateId = node.get("updateId").asLong();
            String shopName = node.get("shop").asText();

            System.out.println("Deleting shop " + shopName);

            List<String> urls = new ArrayList<>();
            if (node.has("urls") && node.get("urls").isArray()) {
                urls = objectMapper.convertValue(node.get("urls"), new TypeReference<List<String>>() {});
            }

            OfferUpdate offerUpdate = offerUpdateRepository.findById(offerUpdateId)
                    .orElseThrow(() -> new IllegalStateException("No OfferUpdate for id=" + offerUpdateId));

            ShopOfferUpdate shopOfferUpdate = shopOfferUpdateRepository
                    .findFirstByOfferUpdate_IdAndShop_NameIgnoreCase(offerUpdateId, shopName)
                    .orElseThrow(() -> new IllegalStateException("No ShopOfferUpdate for OfferUpdate.id=" + offerUpdateId + " and shop=" + shopName));

            offerService.softDeleteByUrls(urls);

            if (offerUpdate.getStartedAt() == null) {
                offerUpdate.setStartedAt(LocalDateTime.now());
            }
            offerUpdate.setFinishedAt(LocalDateTime.now());
            offerUpdateRepository.save(offerUpdate);

            OfferShopUpdateInfoDto.ShopUpdateInfoDto shopUpdateInfo =
                    offerUpdateService.getShopUpdateInfo(shopName, offerUpdateId, false);

            System.out.println("Usunięto oferty dla sklepu " + shopName);
            messagingTemplate.convertAndSend("/topic/offers/" + offerUpdateId, shopUpdateInfo);

        } catch (Exception e) {
            e.printStackTrace();
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
        offerUpdateConfigService.saveOfferUpdateConfig(offerUpdateConfigDto.getIntervalInMinutes(), offerUpdateConfigDto.getType());
        return ResponseEntity.ok(Map.of("message", "Offer update config updated"));
    }

//    @PreAuthorize("hasAuthority('ADMIN')")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/users")
//    public ResponseEntity<List<UserDto>> getUsers(@AuthenticationPrincipal org.example.backend_pcbuild.LoginAndRegister.dto.UserDto userPrincipal) {
    public ResponseEntity<List<UserDto>> getUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
        return ResponseEntity.ok(users);
    }

}



