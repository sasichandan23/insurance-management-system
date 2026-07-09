package com.ims.backend.service;

import com.ims.backend.dto.payment.PaymentResponse;
import com.ims.backend.entity.Payment;
import com.ims.backend.entity.Policy;
import com.ims.backend.entity.User;
import com.ims.backend.entity.enums.PaymentStatus;
import com.ims.backend.entity.enums.PolicyStatus;
import com.ims.backend.entity.enums.Role;
import com.ims.backend.exception.BusinessException;
import com.ims.backend.exception.ResourceNotFoundException;
import com.ims.backend.repository.AgentAssignmentRepository;
import com.ims.backend.repository.PaymentRepository;
import com.ims.backend.repository.PolicyRepository;
import com.ims.backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final AgentAssignmentRepository assignmentRepository;
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    /** Customer pays a due premium (simulated payment). */
    @Transactional
    public PaymentResponse pay(Long paymentId) {
        User customer = currentUserService.getCurrentUser();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (!payment.getPolicy().getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You can only pay premiums of your own policies");
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BusinessException("This premium has already been paid");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidDate(LocalDate.now());
        payment.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 18).toUpperCase());
        payment = paymentRepository.save(payment);

        // Schedule the next premium due, if the policy period allows it
        Policy policy = payment.getPolicy();
        if (policy.getStatus() == PolicyStatus.ACTIVE) {
            LocalDate nextDue = payment.getDueDate()
                    .plusMonths(policy.getPlan().getPremiumFrequency().getMonths());
            if (policy.getEndDate() != null && !nextDue.isAfter(policy.getEndDate())) {
                Payment next = Payment.builder()
                        .policy(policy)
                        .dueDate(nextDue)
                        .amount(policy.getPlan().getPremiumAmount())
                        .build();
                paymentRepository.save(next);
            }
        }

        notificationService.notify(customer, "Payment received",
                "Premium of " + payment.getAmount() + " for policy " + policy.getPolicyNumber()
                        + " received. Transaction: " + payment.getTransactionRef());

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> paymentsOfPolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy", policyId));
        User actor = currentUserService.getCurrentUser();
        switch (actor.getRole()) {
            case CUSTOMER -> {
                if (!policy.getCustomer().getId().equals(actor.getId())) {
                    throw new AccessDeniedException("Not your policy");
                }
            }
            case AGENT -> {
                if (!assignmentRepository.existsByAgentIdAndCustomerId(actor.getId(),
                        policy.getCustomer().getId())) {
                    throw new AccessDeniedException("This customer is not assigned to you");
                }
            }
            case ADMIN -> { /* full access */ }
        }
        return paymentRepository.findByPolicyIdOrderByDueDateDesc(policyId)
                .stream().map(PaymentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> myPayments() {
        User customer = currentUserService.getCurrentUser();
        return paymentRepository.findByPolicyCustomerIdOrderByDueDateDesc(customer.getId())
                .stream().map(PaymentResponse::from).toList();
    }
}
