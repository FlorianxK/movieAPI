package com.dev.movieapi.auth.services;

import com.dev.movieapi.auth.entities.RefreshToken;
import com.dev.movieapi.auth.entities.User;
import com.dev.movieapi.auth.repositories.RefreshTokenRepository;
import com.dev.movieapi.auth.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username).orElseThrow( ()-> new UsernameNotFoundException("Invalid username"));

        RefreshToken refreshToken = user.getRefreshToken();
        if(refreshToken == null) {
            long validityPeriod = 5*60*60*10_000;
            refreshToken = RefreshToken.builder().refreshToken(UUID.randomUUID().toString())
                    .expirationTime(Instant.now().plusMillis(validityPeriod))
                    .user(user)
                    .build();

            refreshTokenRepository.save(refreshToken);
        }

        return refreshToken;
    }

    public RefreshToken verifyRefreshToken(String refreshToken) {
        RefreshToken refToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow( ()-> new RuntimeException("Invalid refresh token") );

        if(refToken.getExpirationTime().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(refToken);
            throw new RuntimeException("Refresh token expired");
        }

        return refToken;
    }
}
