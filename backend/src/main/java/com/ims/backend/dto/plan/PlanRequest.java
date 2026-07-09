package com.ims.backend.dto.plan;

import com.ims.backend.entity.enums.InsuranceType;
import com.ims.backend.entity.enums.PremiumFrequency;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 100, message = "Plan name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "Insurance type is required")
    private InsuranceType insuranceType;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "1000.00", message = "Coverage amount must be at least 1000")
    private BigDecimal coverageAmount;

    @NotNull(message = "Premium amount is required")
    @DecimalMin(value = "1.00", message = "Premium amount must be positive")
    private BigDecimal premiumAmount;

    @NotNull(message = "Premium frequency is required")
    private PremiumFrequency premiumFrequency;

    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 100, message = "Duration must not exceed 100 years")
    private int durationYears;

    private boolean active = true;
}
