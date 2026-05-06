package com.sovereign.domain.user.service;

import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.user.dto.request.ChangePasswordRequest;
import com.sovereign.domain.user.dto.request.UpdateProfileRequest;
import com.sovereign.domain.user.dto.request.UpdateSettingsRequest;
import com.sovereign.domain.user.dto.response.UserResponse;
import com.sovereign.domain.user.dto.response.UserSettingsResponse;
import com.sovereign.domain.user.entity.User;
import com.sovereign.domain.user.entity.UserSettings;
import com.sovereign.domain.user.repository.RefreshTokenRepository;
import com.sovereign.domain.user.repository.UserRepository;
import com.sovereign.domain.user.repository.UserSettingsRepository;
import com.sovereign.exception.exceptions.BadRequestException;
import com.sovereign.exception.exceptions.DuplicateEmailException;
import com.sovereign.exception.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserResponse getProfile(UserDetailsImpl userDetails) {
        // reload fresh from DB
        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return authService.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UserDetailsImpl userDetails, UpdateProfileRequest request) {
        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Email already in use");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        userRepository.save(user);

        log.info("Profile updated for user: {}", user.getId());
        return authService.toUserResponse(user);
    }

    public UserSettingsResponse getSettings(UserDetailsImpl userDetails) {
        UserSettings settings = userSettingsRepository
                .findByUserId(userDetails.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found"));

        return toSettingsResponse(settings);
    }

    @Transactional
    public UserSettingsResponse updateSettings( UserDetailsImpl userDetails, UpdateSettingsRequest request) {
        
        UserSettings settings = userSettingsRepository
                .findByUserId(userDetails.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Settings not found"));

        settings.setCurrency(request.currency());
        settings.setLocale(request.locale());
        settings.setTimezone(request.timezone());
        settings.setTheme(request.theme());
        settings.setNotificationsEnabled(request.notificationsEnabled());
        userSettingsRepository.save(settings);

        log.info("Settings updated for user: {}", userDetails.getUser().getId());
        return toSettingsResponse(settings);
    }

    @Transactional
    public void changePassword(UserDetailsImpl userDetails, ChangePasswordRequest request) {
        User user = userDetails.getUser();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // revoke all refresh tokens — force re-login on all devices
        refreshTokenRepository.deleteByUserId(user.getId());

        log.info("Password changed for user: {}", user.getId());
    }

    @Transactional
    public void deleteAccount(UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        // revoke all tokens first
        refreshTokenRepository.deleteByUserId(user.getId());

        // soft delete — just disable the account
        user.setEnabled(false);
        userRepository.save(user);

        log.info("Account disabled for user: {}", user.getId());
    }

    // ── Helpers ───────────────────────────────────────────────────

    private UserSettingsResponse toSettingsResponse(UserSettings settings) {
        return new UserSettingsResponse(
            settings.getId(),
            settings.getCurrency(),
            settings.getLocale(),
            settings.getTimezone(),
            settings.getTheme(),
            settings.isNotificationsEnabled()
        );
    }
}