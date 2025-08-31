package org.example.backend_pcbuild.LoginAndRegister.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_pcbuild.LoginAndRegister.Repository.RefreshTokenRepository;
import org.example.backend_pcbuild.LoginAndRegister.Service.UserService;
import org.example.backend_pcbuild.models.RefreshToken;
import org.example.backend_pcbuild.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.example.backend_pcbuild.LoginAndRegister.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Component
@RequiredArgsConstructor
public class UserAuthProvider {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Value("${security.jwt.token.secret-key:secret-value}")
    private String secretKey;

    @Value("${security.jwt.refresh-token.expiration:604800000}") // np. 7 dni w ms
    private long refreshTokenExpiration;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(UserDto login) {
        Date now  = new Date();
        Date validity = new Date(now.getTime() + 3_600_000);
//        Date validity = new Date(now.getTime() + 10_000);

        return com.auth0.jwt.JWT.create()
                .withSubject(login.getEmail())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("role", login.getRole().name())
                .withClaim("username", login.getUsername())
                .sign(Algorithm.HMAC256(secretKey));
    }

    @Transactional
    public String createRefreshToken(UserDto login) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenExpiration);

        String refreshToken = JWT.create()
                .withSubject(login.getEmail())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("username", login.getUsername()) // Dodaj username

                .sign(Algorithm.HMAC256(secretKey));

        // Zapisz refresh token w bazie
        RefreshToken byEmail = refreshTokenRepository.findByEmail(login.getEmail());
        if (byEmail != null) {
            refreshTokenRepository.deleteByEmail(login.getEmail()); // Usuń stare tokeny

        }
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setEmail(login.getEmail());
        tokenEntity.setExpiryDate(validity);
        refreshTokenRepository.save(tokenEntity);
        System.out.println("Created refresh token: " + refreshToken + ", expires at: " + validity);

        return refreshToken;
    }

    public Authentication validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            UserDto user = userService.findByLogin(decodedJWT.getSubject());
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token or user not found");
            }


            return new UsernamePasswordAuthenticationToken(user, null, Collections.singletonList(user.getRole()));
        } catch (TokenExpiredException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token has expired", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token", e);}
    }

    public String validateRefreshToken(String refreshToken) {
        System.out.println("Validating refresh token: " + refreshToken);
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);
        if (tokenOpt.isEmpty()) {
            System.out.println("Refresh token not found in database");
            throw new RuntimeException("Refresh token not found in database");
        }
        if (tokenOpt.get().isExpired()) {
            System.out.println("Refresh token has expired: " + tokenOpt.get().getExpiryDate());
            throw new RuntimeException("Refresh token has expired");
        }

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        try {
            DecodedJWT decodedJWT = verifier.verify(refreshToken);
            UserDto user = userService.findByLogin(decodedJWT.getSubject());
            System.out.println("Refresh token validated for user: " + user.getEmail());
            return createToken(user);
        } catch (TokenExpiredException e) {
            System.out.println("TokenExpiredException: " + e.getMessage());
            throw e;
        }
    }
    public void deleteExistingRefreshToken(User user) {
        refreshTokenRepository.deleteByEmail(user.getEmail());
    }

}
