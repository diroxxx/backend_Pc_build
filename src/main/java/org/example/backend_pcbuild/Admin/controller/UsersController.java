package org.example.backend_pcbuild.Admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.UserDto;
import org.example.backend_pcbuild.Admin.dto.UserToShowDto;
import org.example.backend_pcbuild.Admin.dto.UserToUpdate;
import org.example.backend_pcbuild.Admin.service.UserService;
import org.example.backend_pcbuild.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {
    private final UserService userService;


//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserToShowDto>> getUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping
    public ResponseEntity<?> addNewUser(@RequestBody UserToUpdate userToUpdate) {
        if (userService.findUSerByEmail(userToUpdate.getEmail()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Użytkownik nie znaleziony"));
        }
        userService.addUser(userToUpdate);
        return ResponseEntity.ok(Map.of("message", "Użytkownik został dodany"));
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UserToUpdate userToUpdate) {
        if (userService.findUSerByEmail(userToUpdate.getEmail()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Użytkownik nie znaleziony"));
        }
        userService.editUser(userToUpdate);
        return ResponseEntity.ok(Map.of("message", "Użytkownik został zaktualizowany"));

    }
}
