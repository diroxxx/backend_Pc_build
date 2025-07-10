package org.example.backend_pcbuild.Security;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("pc-build/auth")
@RequiredArgsConstructor
public class SecurityController {


    public ResponseEntity<List<String>> message() {
        return ResponseEntity.ok(List.of("Hello World!", "Hello World2!"));
    }


}
