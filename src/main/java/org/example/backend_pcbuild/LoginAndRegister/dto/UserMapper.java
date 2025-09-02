package org.example.backend_pcbuild.LoginAndRegister.dto;

import org.example.backend_pcbuild.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(org.example.backend_pcbuild.models.UserRole.USER)")
    @Mapping(target = "computers", ignore = true)
    @Mapping(target = "password", ignore = true)
    User signUpToUser(SignUpDto signUpDto);


    //dla admina

    org.example.backend_pcbuild.Admin.dto.UserDto toUserDto(org.example.backend_pcbuild.models.User user);
    org.example.backend_pcbuild.models.User toUser(org.example.backend_pcbuild.Admin.dto.UserDto userDto);
}
