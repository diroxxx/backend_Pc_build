package org.example.backend_pcbuild.LoginAndRegister.Service;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserMapper;
import org.example.backend_pcbuild.LoginAndRegister.config.AppException;
import org.example.backend_pcbuild.User;
import org.example.backend_pcbuild.LoginAndRegister.dto.CredentialsDto;
import org.example.backend_pcbuild.LoginAndRegister.dto.SignUpDto;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.example.backend_pcbuild.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDto findByLogin(String login) {
        User user = userRepository.findByEmail(login).orElseThrow(() -> new AppException("Uknown user", HttpStatus.NOT_FOUND));
        return  userMapper.toDto(user);
    }

    public UserDto login(CredentialsDto credentials) {
        User user = userRepository.findByEmail(credentials.getLogin())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        if(!passwordEncoder.matches(CharBuffer.wrap(credentials.getPassword()), user.getPassword())) {

            throw new AppException("Invalid password", HttpStatus.FORBIDDEN);
        }

        return userMapper.toDto(user);
    }

    public UserDto register(SignUpDto userDto) {
        Optional<User> byEmail = userRepository.findByEmail(userDto.getEmail());
        if (byEmail.isPresent()) {
            throw new AppException("Email already in use", HttpStatus.BAD_REQUEST);
        }
        User user = userMapper.signUpToUser(userDto);
        user.setRole(UserRole.USER);
        user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userDto.getPassword())));
        User save = userRepository.save(user);

        return userMapper.toDto(save);
    }
}
