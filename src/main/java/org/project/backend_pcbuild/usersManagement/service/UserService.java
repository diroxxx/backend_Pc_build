package org.project.backend_pcbuild.usersManagement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.project.backend_pcbuild.usersManagement.dto.UserToShowDto;
import org.project.backend_pcbuild.usersManagement.dto.UserToUpdate;
import org.project.backend_pcbuild.loginAndRegister.repository.UserRepository;
import org.project.backend_pcbuild.usersManagement.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<UserToShowDto> findAll() {

        List<User> users = userRepository.findAll();

        List<UserToShowDto> userToUpdateList = new ArrayList<>();
        for (User user : users) {
            UserToShowDto userToUpdate = new UserToShowDto();
            userToUpdate.setId(user.getId());
            userToUpdate.setNickname(user.getUsername());
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setRole(user.getRole());
            userToUpdateList.add(userToUpdate);
        }
        return  userToUpdateList;

    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User findUSerByNickname(String nickname) {
        return userRepository.findByUsername(nickname).orElse(null);
    }

    @Transactional
    public User addUser(UserToUpdate userToUpdate) {
        User user = userRepository.findByEmail(userToUpdate.getEmail()).orElse(null);
        if (user == null) {
            User newUser = new User();
            newUser.setRole(userToUpdate.getRole());
            newUser.setEmail(userToUpdate.getEmail());
            newUser.setUsername(userToUpdate.getNickname());
            newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(userToUpdate.getPassword())));
            return userRepository.save(newUser);
        }
        return null;
    }

    @Transactional
    public void editUser(UserToUpdate userToUpdate) {
        User user = userRepository.findById(userToUpdate.getId()).orElse(null);
        if (user == null) {
            System.out.println("nie znalesiony z email: " + userToUpdate.getEmail());
        }
        if (user != null) {
            if (!userToUpdate.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(CharBuffer.wrap(userToUpdate.getPassword())));
            }
            user.setRole(userToUpdate.getRole());

            if (!userToUpdate.getEmail().isEmpty()) {
                user.setEmail(userToUpdate.getEmail());
            }
            if (!user.getUsername().isEmpty()) {
                user.setUsername(userToUpdate.getNickname());
            }
            userRepository.save(user);
        }
    }
    @Transactional
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony"));
        try {
            userRepository.delete(user);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
