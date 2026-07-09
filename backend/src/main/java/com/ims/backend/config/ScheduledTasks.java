package com.ims.backend.config;

import com.ims.backend.entity.enums.PaymentStatus;
import com.ims.backend.entity.enums.PolicyStatus;
import com.ims.backend.repository.PaymentRepository;
import com.ims.backend.repository.PolicyRepository;
import com.ims.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final PolicyRepository policyRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    /** Runs daily at 01:00: expires ended policies and flags overdue premiums. */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void dailyHousekeeping() {
        expirePolicies();
        flagOverduePayments();
    }

    void expirePolicies() {
        var expired = policyRepository.findByStatusAndEndDateBefore(PolicyStatus.ACTIVE, LocalDate.now());
        for (var policy : expired) {
            policy.setStatus(PolicyStatus.EXPIRED);
            policyRepository.save(policy);
            notificationService.notify(policy.getCustomer(), "Policy expired",
                    "Your policy " + policy.getPolicyNumber() + " has expired.");
        }
        if (!expired.isEmpty()) {
            log.info("Marked {} policies as EXPIRED", expired.size());
        }
    }

    void flagOverduePayments() {
        var overdue = paymentRepository.findByStatusAndDueDateBefore(PaymentStatus.DUE, LocalDate.now());
        for (var payment : overdue) {
            payment.setStatus(PaymentStatus.OVERDUE);
            paymentRepository.save(payment);
            notificationService.notify(payment.getPolicy().getCustomer(), "Premium overdue",
                    "Premium of " + payment.getAmount() + " for policy "
                            + payment.getPolicy().getPolicyNumber() + " is overdue. Please pay at the earliest.");
        }
        if (!overdue.isEmpty()) {
            log.info("Marked {} payments as OVERDUE", overdue.size());
        }
    }
}
