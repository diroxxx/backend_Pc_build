package org.example.backend_pcbuild.Admin.dto;

import lombok.Data;
import org.example.backend_pcbuild.models.User;
import org.example.backend_pcbuild.models.UserRole;

@Data
public class UserToUpdate {
    private Long id;
    private String nickname;
    private String email;
    private String password;
    private UserRole role;


    public UserToUpdate toDto(User user) {
        UserToUpdate userToUpdate = new UserToUpdate();
        userToUpdate.setId(user.getId());
        userToUpdate.setNickname(user.getUsername());
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setRole(user.getRole());
        return userToUpdate;
    }
}
