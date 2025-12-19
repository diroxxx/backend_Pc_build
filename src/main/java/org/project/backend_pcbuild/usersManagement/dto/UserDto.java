package org.project.backend_pcbuild.usersManagement.dto;

import lombok.Data;
import org.project.backend_pcbuild.usersManagement.model.UserRole;

@Data
public class UserDto {
    private String username;
    private String email;
    private UserRole role;
}
