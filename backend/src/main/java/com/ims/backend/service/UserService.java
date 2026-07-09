package com.ims.backend.service;

import com.ims.backend.dto.user.*;
import com.ims.backend.entity.User;
import com.ims.backend.entity.enums.Role;
import com.ims.backend.exception.BusinessException;
import com.ims.backend.exception.DuplicateResourceException;
import com.ims.backend.exception.ResourceNotFoundException;
import com.ims.backend.repository.UserRepository;
import com.ims.backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Role role, String search, int page, int size) {
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        return userRepository.search(role, normalized,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAgents() {
        return userRepository.findByRole(Role.AGENT).stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return UserResponse.from(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id)));
    }

    @Transactional
    public UserResponse createAgent(CreateAgentRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        User agent = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.AGENT)
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        agent = userRepository.save(agent);

        notificationService.notify(agent, "Welcome to IMS",
                "Your agent account has been created. Please change your password after first login.");
        return UserResponse.from(agent);
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (user.getRole() == Role.ADMIN) {
            throw new BusinessException("The admin account cannot be deactivated");
        }
        user.setActive(active);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse myProfile() {
        return UserResponse.from(currentUserService.getCurrentUser());
    }

    @Transactional
    public UserResponse updateMyProfile(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUser();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changeMyPassword(ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
