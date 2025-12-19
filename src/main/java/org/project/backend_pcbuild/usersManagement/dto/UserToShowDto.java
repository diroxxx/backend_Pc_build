package org.project.backend_pcbuild.usersManagement.dto;

import lombok.Data;
import org.project.backend_pcbuild.usersManagement.model.User;
import org.project.backend_pcbuild.usersManagement.model.UserRole;

@Data
public class UserToShowDto {
    private Long id;
    private String nickname;
    private String email;
    private UserRole role;


    public UserToShowDto toDto(User user) {
        UserToShowDto userToUpdate = new UserToShowDto();
        userToUpdate.setId(user.getId());
        userToUpdate.setNickname(user.getUsername());
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setRole(user.getRole());
        return userToUpdate;
    }
}
