package com.ims.backend.dto.assignment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignmentRequest {

    @NotNull(message = "Agent id is required")
    private Long agentId;

    @NotNull(message = "Customer id is required")
    private Long customerId;
}
