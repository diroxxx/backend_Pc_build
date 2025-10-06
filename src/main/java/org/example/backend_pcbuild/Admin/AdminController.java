package org.example.backend_pcbuild.Admin;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.UserDto;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserMapper;
import org.example.backend_pcbuild.Services.ComponentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
//    public Map<String, List<Object>> getOffers() {
    public void getOffers() {
        Map<String, List<Object>> result = componentService.fetchOffersAsMap();
        componentService.saveAllOffers(result);
    }

    @RabbitListener(queues = "offers")
    public void receiveOffer(String message) {
//        System.out.println(message);
        try {
            List<Map<String, Object>> offers = objectMapper.readValue(
                    message, new TypeReference<List<Map<String, Object>>>() {
                    }
            );
            Map<String, Map<String, Integer>> shopCategoryCounts = new HashMap<>();

            for (Map<String, Object> offer : offers) {
                String shop = (String) offer.get("shop");
                String category = (String) offer.get("category");

                if (shop == null || category == null) continue;

                shopCategoryCounts
                        .computeIfAbsent(shop, k -> new HashMap<>())
                        .merge(category, 1, Integer::sum);
            }

            messagingTemplate.convertAndSend("/topic/offers", shopCategoryCounts);
            System.out.println(shopCategoryCounts);
//            shopCategoryCounts.forEach((shop, catMap) -> {
//                System.out.println("Sklep: " + shop);
//                catMap.forEach((cat, count) -> System.out.println("- " + cat + ": " + count));
//            });
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
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



