package com.ims.backend.dto.claim;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClaimRequest {

    @NotNull(message = "Policy id is required")
    private Long policyId;

    @NotNull(message = "Claim amount is required")
    @DecimalMin(value = "1.00", message = "Claim amount must be positive")
    private BigDecimal claimAmount;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be in the future")
    private LocalDate incidentDate;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;
}
