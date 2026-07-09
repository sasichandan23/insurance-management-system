package com.ims.backend.entity;

import com.ims.backend.entity.enums.PolicyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "policies",
        uniqueConstraints = @UniqueConstraint(name = "uk_policies_number", columnNames = "policy_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_number", nullable = false, length = 30)
    private String policyNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_policies_customer"))
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_policies_plan"))
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PolicyStatus status = PolicyStatus.PENDING;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** Type-specific details: nominee (LIFE), family members (HEALTH),
     *  vehicle number (MOTOR), property address (HOME). Stored as JSON text. */
    @Column(name = "details_json", columnDefinition = "TEXT")
    private String detailsJson;

    @Column(length = 255)
    private String remarks;

    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    void onCreate() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
    }
}
