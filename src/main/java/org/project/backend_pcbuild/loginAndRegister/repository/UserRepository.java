package org.project.backend_pcbuild.loginAndRegister.repository;

import org.project.backend_pcbuild.usersManagement.model.User;
import org.project.backend_pcbuild.usersManagement.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User>findByEmail(String email);

    Optional<User> findByEmailAndRole(String email, UserRole role);


}
