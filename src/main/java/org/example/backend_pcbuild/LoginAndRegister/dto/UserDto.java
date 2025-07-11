package org.example.backend_pcbuild.LoginAndRegister.dto;

import lombok.*;
import org.example.backend_pcbuild.UserRole;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String token;
    private UserRole role;

}
