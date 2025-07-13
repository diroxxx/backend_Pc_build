package org.example.backend_pcbuild.LoginAndRegister.Controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.config.UserAuthProvider;
import org.example.backend_pcbuild.LoginAndRegister.Service.UserService;
import org.example.backend_pcbuild.LoginAndRegister.dto.CredentialsDto;
import org.example.backend_pcbuild.LoginAndRegister.dto.SignUpDto;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.example.backend_pcbuild.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    private final UserRepository userRepository;

    @GetMapping("/users")
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody CredentialsDto credentialsDto) {
        UserDto login = userService.login(credentialsDto);
        login.setAccessToken(userAuthProvider.createToken(login));
        login.setRefreshToken( userAuthProvider.createRefreshToken(login));
        return ResponseEntity.ok(login);
    }
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody SignUpDto signUpDto) {

        UserDto user = userService.register(signUpDto);
        user.setRefreshToken(userAuthProvider.createRefreshToken(user));
        return ResponseEntity.created(URI.create("/users" + user.getId())).body(user);
    }


    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        System.out.println("old access token: " + request.getRefreshToken());
        String newAccessToken = userAuthProvider.validateRefreshToken(request.getRefreshToken());

        System.out.println("new access token: " + newAccessToken);
        return ResponseEntity.ok(new LoginResponse(newAccessToken, request.getRefreshToken()));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
    }

    @Data
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}
