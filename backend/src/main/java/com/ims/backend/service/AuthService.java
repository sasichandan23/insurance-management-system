package com.ims.backend.service;

import com.ims.backend.dto.auth.AuthResponse;
import com.ims.backend.dto.auth.LoginRequest;
import com.ims.backend.dto.auth.RegisterRequest;
import com.ims.backend.entity.User;
import com.ims.backend.entity.enums.Role;
import com.ims.backend.exception.DuplicateResourceException;
import com.ims.backend.repository.UserRepository;
import com.ims.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .phone(request.getPhone())
                .address(request.getAddress())
                .dob(request.getDob())
                .build();
        user = userRepository.save(user);

        notificationService.notify(user, "Welcome to IMS",
                "Your account has been created successfully. Browse our insurance plans to get started.");

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
