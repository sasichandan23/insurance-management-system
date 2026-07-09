package com.ims.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ims.backend.dto.policy.PolicyApplyRequest;
import com.ims.backend.dto.policy.PolicyDecisionRequest;
import com.ims.backend.dto.policy.PolicyResponse;
import com.ims.backend.entity.Payment;
import com.ims.backend.entity.Plan;
import com.ims.backend.entity.Policy;
import com.ims.backend.entity.User;
import com.ims.backend.entity.enums.PolicyStatus;
import com.ims.backend.entity.enums.Role;
import com.ims.backend.exception.BusinessException;
import com.ims.backend.exception.ResourceNotFoundException;
import com.ims.backend.repository.AgentAssignmentRepository;
import com.ims.backend.repository.PaymentRepository;
import com.ims.backend.repository.PlanRepository;
import com.ims.backend.repository.PolicyRepository;
import com.ims.backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PolicyRepository policyRepository;
    private final PlanRepository planRepository;
    private final AgentAssignmentRepository assignmentRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    @Transactional
    public PolicyResponse apply(PolicyApplyRequest request) {
        User customer = currentUserService.getCurrentUser();
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", request.getPlanId()));
        if (!plan.isActive()) {
            throw new BusinessException("This plan is no longer available");
        }

        String detailsJson = null;
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            try {
                detailsJson = objectMapper.writeValueAsString(request.getDetails());
            } catch (JsonProcessingException e) {
                throw new BusinessException("Invalid policy details");
            }
        }

        Policy policy = Policy.builder()
                .policyNumber(generateNumber("POL"))
                .customer(customer)
                .plan(plan)
                .status(PolicyStatus.PENDING)
                .detailsJson(detailsJson)
                .build();
        policy = policyRepository.save(policy);

        // Notify the assigned agent, if any
        Policy saved = policy;
        assignmentRepository.findByCustomerId(customer.getId()).ifPresent(assignment ->
                notificationService.notify(assignment.getAgent(), "New policy application",
                        "Customer " + customer.getName() + " applied for policy " + saved.getPolicyNumber()
                                + " (" + plan.getName() + ")."));

        return PolicyResponse.from(policy);
    }

    @Transactional
    public PolicyResponse decide(Long policyId, PolicyDecisionRequest request) {
        User actor = currentUserService.getCurrentUser();
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));

        if (policy.getStatus() != PolicyStatus.PENDING) {
            throw new BusinessException("Only PENDING policies can be approved or rejected");
        }
        assertAgentOwnsCustomerOrAdmin(actor, policy.getCustomer().getId());

        if (Boolean.TRUE.equals(request.getApproved())) {
            policy.setStatus(PolicyStatus.ACTIVE);
            policy.setStartDate(LocalDate.now());
            policy.setEndDate(LocalDate.now().plusYears(policy.getPlan().getDurationYears()));
            policy.setRemarks(request.getRemarks());
            policy = policyRepository.save(policy);

            // First premium becomes due immediately
            Payment firstDue = Payment.builder()
                    .policy(policy)
                    .dueDate(policy.getStartDate())
                    .amount(policy.getPlan().getPremiumAmount())
                    .build();
            paymentRepository.save(firstDue);

            notificationService.notify(policy.getCustomer(), "Policy approved",
                    "Your policy " + policy.getPolicyNumber() + " is now ACTIVE. The first premium of "
                            + policy.getPlan().getPremiumAmount() + " is due today.");
        } else {
            policy.setStatus(PolicyStatus.REJECTED);
            policy.setRemarks(request.getRemarks());
            policy = policyRepository.save(policy);

            notificationService.notify(policy.getCustomer(), "Policy rejected",
                    "Your policy application " + policy.getPolicyNumber() + " was rejected."
                            + (request.getRemarks() != null ? " Reason: " + request.getRemarks() : ""));
        }
        return PolicyResponse.from(policy);
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> myPolicies() {
        User customer = currentUserService.getCurrentUser();
        return policyRepository.findByCustomerIdOrderByAppliedAtDesc(customer.getId())
                .stream().map(PolicyResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<PolicyResponse> listPolicies(PolicyStatus status, int page, int size) {
        User actor = currentUserService.getCurrentUser();
        PageRequest pageable = PageRequest.of(page, size);
        if (actor.getRole() == Role.AGENT) {
            return policyRepository.findForAgent(actor.getId(), status, pageable).map(PolicyResponse::from);
        }
        return policyRepository.findAllByOptionalStatus(status, pageable).map(PolicyResponse::from);
    }

    @Transactional(readOnly = true)
    public PolicyResponse getPolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", id));
        assertCanView(currentUserService.getCurrentUser(), policy);
        return PolicyResponse.from(policy);
    }

    /** Customer requests cancellation; the admin is notified and confirms via cancel(). */
    @Transactional
    public void requestCancellation(Long id) {
        User customer = currentUserService.getCurrentUser();
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", id));
        if (!policy.getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("Not your policy");
        }
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new BusinessException("Only ACTIVE policies can be cancelled");
        }
        notificationService.notifyAdmins("Cancellation request",
                "Customer " + customer.getName() + " requested cancellation of policy "
                        + policy.getPolicyNumber() + ".");
    }

    /** Admin confirms a cancellation. */
    @Transactional
    public PolicyResponse cancel(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", id));
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new BusinessException("Only ACTIVE policies can be cancelled");
        }
        policy.setStatus(PolicyStatus.CANCELLED);
        policy = policyRepository.save(policy);
        notificationService.notify(policy.getCustomer(), "Policy cancelled",
                "Your policy " + policy.getPolicyNumber() + " has been cancelled.");
        return PolicyResponse.from(policy);
    }

    // ---------- helpers ----------

    private void assertCanView(User actor, Policy policy) {
        switch (actor.getRole()) {
            case ADMIN -> { /* full access */ }
            case CUSTOMER -> {
                if (!policy.getCustomer().getId().equals(actor.getId())) {
                    throw new AccessDeniedException("Not your policy");
                }
            }
            case AGENT -> assertAgentOwnsCustomerOrAdmin(actor, policy.getCustomer().getId());
        }
    }

    private void assertAgentOwnsCustomerOrAdmin(User actor, Long customerId) {
        if (actor.getRole() == Role.ADMIN) {
            return;
        }
        if (actor.getRole() != Role.AGENT
                || !assignmentRepository.existsByAgentIdAndCustomerId(actor.getId(), customerId)) {
            throw new AccessDeniedException("This customer is not assigned to you");
        }
    }

    static String generateNumber(String prefix) {
        return prefix + "-" + System.currentTimeMillis() + "-" + (100 + RANDOM.nextInt(900));
    }
}
