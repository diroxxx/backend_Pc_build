package org.example.backend_pcbuild.LoginAndRegister.Repository;

import org.example.backend_pcbuild.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User>findByEmail(String email);
}
