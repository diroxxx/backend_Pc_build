package org.example.backend_pcbuild.Admin;


import lombok.AllArgsConstructor;
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

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("http://127.0.0.1:5000")
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final ComponentService componentService;
    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;


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
//    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(value = "/offers",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<Object>>> getOffers() {
        Map<String, List<Object>> result = componentService.fetchOffersAsMap();
        componentService.saveAllOffers(result);
        System.out.println();
        return ResponseEntity.ok(result);
    }

    @RabbitListener(queues = "olx")
    public void receiveOffer(String message) {
        System.out.println(message);
        messagingTemplate.convertAndSend("/topic/offers", message);
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



