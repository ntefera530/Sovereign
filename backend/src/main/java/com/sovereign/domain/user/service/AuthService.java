package com.sovereign.domain.user.service;

import com.sovereign.config.security.JwtService;
import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.user.dto.request.LoginRequest;
import com.sovereign.domain.user.dto.request.RefreshTokenRequest;
import com.sovereign.domain.user.dto.request.RegisterRequest;
import com.sovereign.domain.user.dto.response.AuthResponse;
import com.sovereign.domain.user.dto.response.UserResponse;
import com.sovereign.domain.user.entity.RefreshToken;
import com.sovereign.domain.user.entity.User;
import com.sovereign.domain.user.entity.UserSettings;
import com.sovereign.domain.user.repository.RefreshTokenRepository;
import com.sovereign.domain.user.repository.UserRepository;
import com.sovereign.domain.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Does email already exist?
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Email already in use");
        }

        // Create user
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        userRepository.save(user);

        // Create default settings
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        userSettingsRepository.save(settings);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = saveRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, toUserResponse(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));    

        // Revoke existing refresh tokens
        refreshTokenRepository.deleteByUserId(user.getId());

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = saveRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, toUserResponse(user));
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));
    
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }

        String email = jwtService.extractEmail(request.refreshToken());
        String newAccessToken = jwtService.generateAccessToken(email);

        return new AuthResponse(newAccessToken, request.refreshToken(), toUserResponse(stored.getUser()));
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user ->
            refreshTokenRepository.deleteByUserId(user.getId())
        );
    }

    // Helpers 

    private String saveRefreshToken(User user) {
        String token = jwtService.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.isEmailVerified(),
            user.getCreatedAt()
        );
    }
}