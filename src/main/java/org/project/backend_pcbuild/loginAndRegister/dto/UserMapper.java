package org.project.backend_pcbuild.loginAndRegister.dto;

import org.project.backend_pcbuild.usersManagement.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(org.project.backend_pcbuild.usersManagement.model.UserRole.USER)")
    @Mapping(target = "computers", ignore = true)
    @Mapping(target = "password", ignore = true)
    User signUpToUser(SignUpDto signUpDto);


}
