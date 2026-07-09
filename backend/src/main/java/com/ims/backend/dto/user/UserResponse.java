package com.ims.backend.dto.user;

import com.ims.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String address;
    private LocalDate dob;
    private boolean active;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .address(user.getAddress())
                .dob(user.getDob())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
