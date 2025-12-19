package org.project.backend_pcbuild.usersManagement.dto;

import lombok.Data;
import org.project.backend_pcbuild.usersManagement.model.User;
import org.project.backend_pcbuild.usersManagement.model.UserRole;

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
