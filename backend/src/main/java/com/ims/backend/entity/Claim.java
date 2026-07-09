package com.ims.backend.entity;

import com.ims.backend.entity.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claims",
        uniqueConstraints = @UniqueConstraint(name = "uk_claims_number", columnNames = "claim_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", nullable = false, length = 30)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_claims_policy"))
    private Policy policy;

    @Column(name = "claim_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.FILED;

    @Column(name = "reviewer_remarks", length = 255)
    private String reviewerRemarks;

    @Column(name = "settled_amount", precision = 15, scale = 2)
    private BigDecimal settledAmount;

    @Column(name = "settled_date")
    private LocalDate settledDate;

    @Column(name = "filed_at", nullable = false, updatable = false)
    private LocalDateTime filedAt;

    @PrePersist
    void onCreate() {
        if (filedAt == null) {
            filedAt = LocalDateTime.now();
        }
    }
}
