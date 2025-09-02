package org.example.backend_pcbuild.Admin.dto;

import lombok.Data;
import org.example.backend_pcbuild.models.UserRole;

@Data
public class UserDto {
    private String username;
    private String email;
    private UserRole role;
}
