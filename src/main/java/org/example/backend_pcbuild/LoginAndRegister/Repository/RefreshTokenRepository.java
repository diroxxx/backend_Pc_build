package org.example.backend_pcbuild.LoginAndRegister.Repository;

import org.example.backend_pcbuild.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByEmail(String email);

    RefreshToken findByEmail(String email);
}
