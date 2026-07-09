package com.ims.backend.dto.policy;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PolicyDecisionRequest {

    @NotNull(message = "Decision (approved true/false) is required")
    private Boolean approved;

    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    private String remarks;
}
