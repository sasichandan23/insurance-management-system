package com.ims.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a 10-digit number")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
}
