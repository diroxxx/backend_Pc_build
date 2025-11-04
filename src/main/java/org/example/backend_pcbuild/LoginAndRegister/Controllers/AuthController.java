package org.example.backend_pcbuild.LoginAndRegister.Controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.configuration.JwtConfig.UserAuthProvider;
import org.example.backend_pcbuild.LoginAndRegister.Service.UserService;
import org.example.backend_pcbuild.LoginAndRegister.dto.CredentialsDto;
import org.example.backend_pcbuild.LoginAndRegister.dto.SignUpDto;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.example.backend_pcbuild.models.User;
import org.example.backend_pcbuild.models.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private final UserService userService;
    private final UserAuthProvider userAuthProvider;

    private final UserRepository userRepository;


@PostMapping("/login/user")
public ResponseEntity<UserDto> loginUser(@RequestBody CredentialsDto credentialsDto, HttpServletResponse response) {
    UserDto login = userService.login(credentialsDto, UserRole.USER);
    login.setAccessToken(userAuthProvider.createToken(login));
    String refreshToken = userAuthProvider.createRefreshToken(login);

    Cookie cookie = new Cookie("refresh_token", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    response.addCookie(cookie);

    return ResponseEntity.ok(login);
}

    @PostMapping("/login/admin")
    public ResponseEntity<UserDto> loginAdmin(@RequestBody CredentialsDto credentialsDto, HttpServletResponse response) {
        UserDto login = userService.login(credentialsDto, UserRole.ADMIN);
        login.setAccessToken(userAuthProvider.createToken(login));
        String refreshToken = userAuthProvider.createRefreshToken(login);

        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(login);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody SignUpDto signUpDto) {

        UserDto user = userService.register(signUpDto);
        user.setRefreshToken(userAuthProvider.createRefreshToken(user));
        return ResponseEntity.created(URI.create("/users" + user.getId())).body(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String newAccessToken = userAuthProvider.validateRefreshToken(refreshToken);
        if (newAccessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new LoginResponse(newAccessToken, refreshToken));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyCurrentPassword(@RequestBody PasswordChangeRequest request, Authentication authentication) {
        UserDto user = (UserDto) authentication.getPrincipal();
        String email = user.getEmail();
        boolean isValid = userService.checkPassword(email, request.getCurrentPassword());
        if (isValid) {
            return ResponseEntity.ok("Password verified");
        }
        return ResponseEntity.badRequest().body("Invalid password");
    }

    @PreAuthorize("hasAuthority('USER')")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request,  Authentication authentication) {
//        String userEmail = authentication.getName();
        UserDto user = (UserDto) authentication.getPrincipal();
        String email = user.getEmail();
        User userAfterChanged = userService.changePassword(email, request.getCurrentPassword());
        System.out.println( "hasło do zmiany " +request.getCurrentPassword());
        if (userAfterChanged == null) {
            return ResponseEntity.badRequest().body("Password have not been changed");
        }
        return ResponseEntity.ok("Password changed");
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PasswordChangeRequest {
        private String currentPassword;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
    }

}
