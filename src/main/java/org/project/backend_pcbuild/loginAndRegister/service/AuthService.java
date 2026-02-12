package org.project.backend_pcbuild.loginAndRegister.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.project.backend_pcbuild.loginAndRegister.repository.UserRepository;
import org.project.backend_pcbuild.loginAndRegister.dto.UserMapper;
import org.project.backend_pcbuild.configuration.jwtConfig.AppException;
import org.project.backend_pcbuild.usersManagement.model.User;
import org.project.backend_pcbuild.loginAndRegister.dto.CredentialsDto;
import org.project.backend_pcbuild.loginAndRegister.dto.SignUpDto;
import org.project.backend_pcbuild.loginAndRegister.dto.UserDto;
import org.project.backend_pcbuild.usersManagement.model.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDto findByLogin(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(userMapper::toDto).orElse(null);
    }

    public UserDto login(CredentialsDto credentials, UserRole userRole) {

        User user = userRepository.findByEmailAndRole(credentials.getLogin(), userRole)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if(!passwordEncoder.matches(CharBuffer.wrap(credentials.getPassword()), user.getPassword())) {

            throw new AppException("Invalid password or Email", HttpStatus.FORBIDDEN);
        }

        return userMapper.toDto(user);
    }

    public boolean checkPassword(String login, String password) {
        User user = userRepository.findByEmail(login).orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        return passwordEncoder.matches(CharBuffer.wrap(password), user.getPassword());
    }
    @Transactional
    public User changePassword(String login,  String newPassword) {
        User user = userRepository.findByEmail(login).orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        user.setPassword(passwordEncoder.encode(newPassword));

        System.out.println("nowe hasło " + newPassword);
//        System.out.println("nowe hasło " + user.getPassword());
        return userRepository.save(user);
    }


    public UserDto register(SignUpDto userDto) {
        Optional<User> byEmail = userRepository.findByEmail(userDto.getEmail());
        if (byEmail.isPresent()) {
            throw new AppException("Email already in use", HttpStatus.BAD_REQUEST);
        }

        if (userDto.getUsername() != null && userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new AppException("Username already in use", HttpStatus.BAD_REQUEST);
        }

        if (userDto.getPassword() != null && userDto.getPassword().length < 8) {
            throw new AppException("Password must be at least 8 characters long", HttpStatus.BAD_REQUEST);
        }

        User user = userMapper.signUpToUser(userDto);
        user.setRole(UserRole.USER);
        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDto.getPassword())));
        User save = userRepository.save(user);

        return userMapper.toDto(save);
    }
}
