package com.ims.backend.dto.claim;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClaimSettleRequest {

    @NotNull(message = "Settled amount is required")
    @DecimalMin(value = "0.00", message = "Settled amount cannot be negative")
    private BigDecimal settledAmount;
}
