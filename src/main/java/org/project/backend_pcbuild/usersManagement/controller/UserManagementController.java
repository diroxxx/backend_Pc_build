package org.project.backend_pcbuild.usersManagement.controller;

import lombok.RequiredArgsConstructor;
import org.project.backend_pcbuild.usersManagement.dto.UserToShowDto;
import org.project.backend_pcbuild.usersManagement.dto.UserToUpdate;
import org.project.backend_pcbuild.usersManagement.service.UserService;
import org.project.backend_pcbuild.usersManagement.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {
    private final UserService userService;


//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserToShowDto>> getUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping
    public ResponseEntity<?> addNewUser(@RequestBody UserToUpdate userToUpdate) {
        System.out.println(userToUpdate);
        if (userService.findUserByEmail(userToUpdate.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Użytkownik o podanym emailu już istnieje"));
        }

        if (userService.findUSerByNickname(userToUpdate.getNickname()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Użytkownik o podanym pseudonimie już istnieje"));
        }

        userService.addUser(userToUpdate);
        return ResponseEntity.ok(Map.of("message", "Użytkownik został dodany"));
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UserToUpdate userToUpdate) {
        System.out.println(userToUpdate);
        if (userService.findUserById(userToUpdate.getId()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Użytkownik nie znaleziony"));
        }
        userService.editUser(userToUpdate);
        return ResponseEntity.ok(Map.of("message", "Użytkownik został zaktualizowany"));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping
    public ResponseEntity<?> deleteUserByEmail(@RequestParam("email") String email) {
        boolean existed = userService.findUserByEmail(email) != null;
        if (!existed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Użytkownik nie znaleziony"));
        }
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok(Map.of("message", "Użytkownik został usunięty"));
    }


    @GetMapping("/check")
    public ResponseEntity<?> checkUserNicknameAndEmailValid(@RequestParam("nickname") String nickname,
                                                       @RequestParam("email") String email) {
        if(nickname != null &&  !nickname.isBlank()) {
            User user = userService.findUSerByNickname(nickname);
            if (user != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Pseudonim już istnieje"));
            }
        }
        if (email != null && !email.isBlank()) {
            User user = userService.findUserByEmail(email);
            if (user != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Email już istnieje"));
            }
        }
        return ResponseEntity.ok().body(Map.of("message", "Dane są dostępne"));
    }
}
