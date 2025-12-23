package org.project.backend_pcbuild.unitTests;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.backend_pcbuild.loginAndRegister.repository.UserRepository;
import org.project.backend_pcbuild.usersManagement.dto.UserToUpdate;
import org.project.backend_pcbuild.usersManagement.model.User;
import org.project.backend_pcbuild.usersManagement.model.UserRole;
import org.project.backend_pcbuild.usersManagement.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser() {
        UserToUpdate userToUpdate = new UserToUpdate();
        userToUpdate.setEmail("existing@domain.com");
        userToUpdate.setNickname("existingUser");
        userToUpdate.setPassword("password123");
        userToUpdate.setRole(UserRole.USER);

        when(userRepository.findByEmail(userToUpdate.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("password123");


        User userToCheck = new User();
        userToCheck.setEmail(userToUpdate.getEmail());
        userToCheck.setUsername(userToUpdate.getNickname());
        userToCheck.setPassword("encodedPassword");
        userToCheck.setRole(userToUpdate.getRole());

        when(userRepository.save(any(User.class))).thenReturn(userToCheck);

        User result = userService.addUser(userToUpdate);

        assertNotNull(result);
        assertEquals(userToCheck.getUsername(), result.getUsername());
        assertEquals(userToCheck.getEmail(), result.getEmail());
        assertEquals(userToCheck.getPassword(), result.getPassword());

    }

    @Test
    void shouldNotCreateUserIfEmailAlreadyExists() {
        UserToUpdate userToUpdate = new UserToUpdate();
        userToUpdate.setEmail("existing@domain.com");
        userToUpdate.setNickname("existingUser");
        userToUpdate.setPassword("password123");
        userToUpdate.setRole(UserRole.USER);

        User existingUser = new User();
        existingUser.setEmail("existing@domain.com");
        when(userRepository.findByEmail(userToUpdate.getEmail())).thenReturn(java.util.Optional.of(existingUser));

        User result = userService.addUser(userToUpdate);

        assertEquals(null, result);
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }


    @Test
    void shouldReturnNullWhenUserAlreadyExists() {
        UserToUpdate userToUpdate = new UserToUpdate();
        userToUpdate.setEmail("already@existing.com");
        userToUpdate.setNickname("alreadyExistingUser");
        userToUpdate.setPassword("securePassword");
        userToUpdate.setRole(UserRole.USER);

        when(userRepository.findByEmail("already@existing.com"))
                .thenReturn(java.util.Optional.of(new User()));

        User result = userService.addUser(userToUpdate);

        assertEquals(null, result);
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void shouldDeleteUserByEmailSuccessfully() {
        String email = "test@domain.com";

        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        userService.deleteUserByEmail(email);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        String email = "nonexistent@domain.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.deleteUserByEmail(email));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void shouldNotCallDeleteIfUserIsNotFound() {
        String email = "notfound@domain.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        try {
            userService.deleteUserByEmail(email);
        } catch (ResponseStatusException ignored) {
        }

        verify(userRepository, never()).delete(any());
    }
}
