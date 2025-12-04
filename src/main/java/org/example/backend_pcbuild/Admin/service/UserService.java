package org.example.backend_pcbuild.Admin.service;

import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.Admin.dto.UserToShowDto;
import org.example.backend_pcbuild.Admin.dto.UserToUpdate;
import org.example.backend_pcbuild.LoginAndRegister.Repository.UserRepository;
import org.example.backend_pcbuild.models.User;
import org.example.backend_pcbuild.models.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public List<UserToShowDto> findAll() {

        List<User> users = userRepository.findAll();

        List<UserToShowDto> userToUpdateList = new ArrayList<>();
        for (User user : users) {
            UserToShowDto userToUpdate = new UserToShowDto();
            userToUpdate.setNickname(user.getUsername());
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setRole(user.getRole());
            userToUpdateList.add(userToUpdate);
        }
        return  userToUpdateList;

    }

    public User findUSerByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void addUser(UserToUpdate userToUpdate) {
        User user = userRepository.findByEmail(userToUpdate.getEmail()).orElse(null);
        if (user == null) {
            User newUser = new User();
            newUser.setRole(userToUpdate.getRole());
            newUser.setEmail(userToUpdate.getEmail());
            newUser.setUsername(userToUpdate.getEmail());
            newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(userToUpdate.getPassword())));
        }
    }

    public void editUser(UserToUpdate userToUpdate) {
        User user = userRepository.findByEmail(userToUpdate.getEmail()).orElse(null);
        if (user == null) {
            System.out.println("nie znalesiony z email: " + userToUpdate.getEmail());
        }
        if (user != null) {
            System.out.println(userToUpdate.getEmail());
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

}
