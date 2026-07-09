package com.ims.backend.service;

import com.ims.backend.dto.payment.PaymentResponse;
import com.ims.backend.entity.User;
import com.ims.backend.entity.enums.ClaimStatus;
import com.ims.backend.entity.enums.PaymentStatus;
import com.ims.backend.entity.enums.PolicyStatus;
import com.ims.backend.entity.enums.Role;
import com.ims.backend.repository.*;
import com.ims.backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PaymentRepository paymentRepository;
    private final AgentAssignmentRepository assignmentRepository;
    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        User user = currentUserService.getCurrentUser();
        return switch (user.getRole()) {
            case ADMIN -> adminSummary();
            case AGENT -> agentSummary(user);
            case CUSTOMER -> customerSummary(user);
        };
    }

    private Map<String, Object> adminSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCustomers", userRepository.countByRole(Role.CUSTOMER));
        summary.put("totalAgents", userRepository.countByRole(Role.AGENT));
        summary.put("activePolicies", policyRepository.countByStatus(PolicyStatus.ACTIVE));
        summary.put("pendingPolicies", policyRepository.countByStatus(PolicyStatus.PENDING));

        Map<String, Long> claims = new LinkedHashMap<>();
        for (ClaimStatus status : ClaimStatus.values()) {
            claims.put(status.name(), claimRepository.countByStatus(status));
        }
        summary.put("claimsByStatus", claims);

        BigDecimal collected = paymentRepository.sumAmountByStatus(PaymentStatus.PAID);
        summary.put("totalPremiumCollected", collected == null ? BigDecimal.ZERO : collected);
        return summary;
    }

    private Map<String, Object> agentSummary(User agent) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("assignedCustomers", assignmentRepository.countByAgentId(agent.getId()));
        summary.put("pendingPolicies",
                policyRepository.countForAgentByStatus(agent.getId(), PolicyStatus.PENDING));
        summary.put("activePolicies",
                policyRepository.countForAgentByStatus(agent.getId(), PolicyStatus.ACTIVE));
        long awaitingReview = claimRepository.countForAgentByStatus(agent.getId(), ClaimStatus.FILED)
                + claimRepository.countForAgentByStatus(agent.getId(), ClaimStatus.UNDER_REVIEW);
        summary.put("claimsAwaitingReview", awaitingReview);
        summary.put("recentPayments", paymentRepository.findForAgent(agent.getId()).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .limit(5)
                .map(PaymentResponse::from)
                .toList());
        return summary;
    }

    private Map<String, Object> customerSummary(User customer) {
        Map<String, Object> summary = new LinkedHashMap<>();
        var policies = policyRepository.findByCustomerIdOrderByAppliedAtDesc(customer.getId());
        summary.put("totalPolicies", policies.size());
        summary.put("activePolicies",
                policies.stream().filter(p -> p.getStatus() == PolicyStatus.ACTIVE).count());
        summary.put("pendingPolicies",
                policies.stream().filter(p -> p.getStatus() == PolicyStatus.PENDING).count());

        // Next premium due (earliest unpaid due)
        var nextDue = paymentRepository.findByPolicyCustomerIdOrderByDueDateDesc(customer.getId()).stream()
                .filter(p -> p.getStatus() != PaymentStatus.PAID)
                .min(Comparator.comparing(p -> p.getDueDate()))
                .map(PaymentResponse::from)
                .orElse(null);
        summary.put("nextPremiumDue", nextDue);

        summary.put("totalClaims",
                claimRepository.findByPolicyCustomerIdOrderByFiledAtDesc(customer.getId()).size());
        summary.put("unreadNotifications",
                notificationRepository.countByUserIdAndReadFalse(customer.getId()));
        summary.put("today", LocalDate.now());
        return summary;
    }
}
