package org.project.backend_pcbuild.loginAndRegister.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.project.backend_pcbuild.usersManagement.model.UserRole;

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
