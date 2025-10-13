package org.example.backend_pcbuild.Admin;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.*;
import org.example.backend_pcbuild.Admin.service.OfferUpdateConfigService;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserMapper;
import org.example.backend_pcbuild.Services.ComponentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://127.0.0.1:5000")
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ComponentService componentService;
    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private final OfferUpdateConfigService offerUpdateConfigService;


    private final UserMapper userMapper;
//        @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(value = "/components",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Object>>> getComponents() {
        Map<String, List<Object>> result = componentService.fetchComponentsAsMap();
        componentService.saveBasedComponents(result);

        return ResponseEntity.ok(result);
    }
    @MessageMapping("/offers")
    @SendTo("/topic/offers")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void getOffers(@Payload ManualFetchOffersSettingsDto settings) {
        Map<String, List<Object>> result = componentService.fetchOffersAsMap(settings.getShops());
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

    @Transactional
    @RabbitListener(queues = "offers")
    public void receiveOffer(String message) {
        try {
            List<ScrapingOfferDto> scrapingResults = objectMapper.readValue(
                    message, new TypeReference<List<ScrapingOfferDto>>() {
                    }
            );

            Map<String, Map<String, Integer>> shopCategoryCounts = new HashMap<>();
            Map<String, List<Object>> offersByCategory = new HashMap<>();

            for (ScrapingOfferDto result : scrapingResults) {
                if (result.getComponentsData() == null) continue;

                for (ComponentOfferDto offer : result.getComponentsData()) {
                    String category = offer.getCategory();
                    if (category == null) continue;

                    // Convert DTO to Map for existing saveAllOffers method
                    Map<String, Object> offerMap = convertOfferDtoToMap(offer);
                    offersByCategory
                            .computeIfAbsent(category, k -> new java.util.ArrayList<>())
                            .add(offerMap);

                    String shop = offer.getShop();
                    if (shop != null) {
                        shopCategoryCounts
                                .computeIfAbsent(shop, k -> new HashMap<>())
                                .merge(category, 1, Integer::sum);
                    }
                }
            }

            componentService.saveAllOffers(offersByCategory);
            messagingTemplate.convertAndSend("/topic/offers", shopCategoryCounts);
            System.out.println(shopCategoryCounts);

        } catch (Exception e) {
            System.out.println("Error processing offers: " + e.getMessage());
            e.printStackTrace();
        }
    }



        @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/OfferUpdateConfig")
    public ResponseEntity<?> updateOfferConfig(@RequestBody OfferUpdateConfigDto offerUpdateConfigDto) {

        if (offerUpdateConfigDto.getType() == OfferUpdateType.AUTOMATIC
                && offerUpdateConfigDto.getIntervalInMinutes() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Automatic update interval cannot be null"));        }
        offerUpdateConfigService.saveOfferUpdateConfig(offerUpdateConfigDto.getIntervalInMinutes(), offerUpdateConfigDto.getType());
        return ResponseEntity.ok(Map.of("message", "Offer update config updated"));
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
        return ResponseEntity.ok(users);
    }

}



