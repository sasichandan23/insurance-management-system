package com.ims.backend.security;

import com.ims.backend.entity.User;
import com.ims.backend.entity.enums.Role;
import com.ims.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Resolves the currently authenticated user from the security context. */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UsernameNotFoundException("No authenticated user");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + auth.getName()));
    }

    public boolean isAdmin() {
        return getCurrentUser().getRole() == Role.ADMIN;
    }
}
