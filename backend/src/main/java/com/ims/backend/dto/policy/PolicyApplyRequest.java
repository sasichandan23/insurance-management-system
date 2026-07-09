package com.ims.backend.dto.policy;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class PolicyApplyRequest {

    @NotNull(message = "Plan id is required")
    private Long planId;

    /** Type-specific details: nominee (LIFE), family members (HEALTH),
     *  vehicle registration number (MOTOR), property address (HOME). */
    private Map<String, Object> details;
}
