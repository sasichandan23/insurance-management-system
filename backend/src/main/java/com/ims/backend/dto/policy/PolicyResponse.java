package com.ims.backend.dto.policy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ims.backend.entity.Policy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Long id;
    private String policyNumber;
    private Long customerId;
    private String customerName;
    private Long planId;
    private String planName;
    private String insuranceType;
    private BigDecimal coverageAmount;
    private BigDecimal premiumAmount;
    private String premiumFrequency;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Object> details;
    private String remarks;
    private LocalDateTime appliedAt;

    @SuppressWarnings("unchecked")
    public static PolicyResponse from(Policy policy) {
        Map<String, Object> details = null;
        if (policy.getDetailsJson() != null && !policy.getDetailsJson().isBlank()) {
            try {
                details = MAPPER.readValue(policy.getDetailsJson(), Map.class);
            } catch (JsonProcessingException ignored) {
                // keep details null if stored JSON is unreadable
            }
        }
        return PolicyResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .customerId(policy.getCustomer().getId())
                .customerName(policy.getCustomer().getName())
                .planId(policy.getPlan().getId())
                .planName(policy.getPlan().getName())
                .insuranceType(policy.getPlan().getInsuranceType().name())
                .coverageAmount(policy.getPlan().getCoverageAmount())
                .premiumAmount(policy.getPlan().getPremiumAmount())
                .premiumFrequency(policy.getPlan().getPremiumFrequency().name())
                .status(policy.getStatus().name())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .details(details)
                .remarks(policy.getRemarks())
                .appliedAt(policy.getAppliedAt())
                .build();
    }
}
