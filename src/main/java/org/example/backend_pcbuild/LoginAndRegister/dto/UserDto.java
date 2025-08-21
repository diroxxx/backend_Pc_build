package org.example.backend_pcbuild.LoginAndRegister.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.example.backend_pcbuild.models.UserRole;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;
    private String accessToken;
    private String refreshToken;
    private UserRole role;

}
