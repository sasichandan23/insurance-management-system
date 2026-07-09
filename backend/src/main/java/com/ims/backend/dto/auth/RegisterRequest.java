package com.ims.backend.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 60, message = "Password must be between 8 and 60 characters")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a 10-digit number")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;
}
