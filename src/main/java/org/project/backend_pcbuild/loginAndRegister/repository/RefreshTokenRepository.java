package org.project.backend_pcbuild.loginAndRegister.repository;

import org.project.backend_pcbuild.loginAndRegister.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByEmail(String email);

    RefreshToken findByEmail(String email);
}
