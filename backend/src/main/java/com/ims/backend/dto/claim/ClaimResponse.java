package com.ims.backend.dto.claim;

import com.ims.backend.entity.Claim;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    private Long id;
    private String claimNumber;
    private Long policyId;
    private String policyNumber;
    private String planName;
    private String customerName;
    private BigDecimal claimAmount;
    private LocalDate incidentDate;
    private String description;
    private String status;
    private String reviewerRemarks;
    private BigDecimal settledAmount;
    private LocalDate settledDate;
    private LocalDateTime filedAt;

    public static ClaimResponse from(Claim claim) {
        return ClaimResponse.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .policyId(claim.getPolicy().getId())
                .policyNumber(claim.getPolicy().getPolicyNumber())
                .planName(claim.getPolicy().getPlan().getName())
                .customerName(claim.getPolicy().getCustomer().getName())
                .claimAmount(claim.getClaimAmount())
                .incidentDate(claim.getIncidentDate())
                .description(claim.getDescription())
                .status(claim.getStatus().name())
                .reviewerRemarks(claim.getReviewerRemarks())
                .settledAmount(claim.getSettledAmount())
                .settledDate(claim.getSettledDate())
                .filedAt(claim.getFiledAt())
                .build();
    }
}
