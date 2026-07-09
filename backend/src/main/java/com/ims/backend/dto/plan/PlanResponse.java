package com.ims.backend.dto.plan;

import com.ims.backend.entity.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {

    private Long id;
    private String name;
    private String insuranceType;
    private String description;
    private BigDecimal coverageAmount;
    private BigDecimal premiumAmount;
    private String premiumFrequency;
    private int durationYears;
    private boolean active;

    public static PlanResponse from(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .insuranceType(plan.getInsuranceType().name())
                .description(plan.getDescription())
                .coverageAmount(plan.getCoverageAmount())
                .premiumAmount(plan.getPremiumAmount())
                .premiumFrequency(plan.getPremiumFrequency().name())
                .durationYears(plan.getDurationYears())
                .active(plan.isActive())
                .build();
    }
}
