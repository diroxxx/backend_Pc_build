package org.example.backend_pcbuild;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BackendPcBuildApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendPcBuildApplication.class, args);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password04";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Hasło zaszyfrowane: " + encodedPassword);
    }

}
