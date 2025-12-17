package org.example.backend_pcbuild.Admin.dto;

import lombok.Data;
import org.example.backend_pcbuild.models.User;
import org.example.backend_pcbuild.models.UserRole;

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
