package com.ims.backend.repository;

import com.ims.backend.entity.Payment;
import com.ims.backend.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPolicyIdOrderByDueDateDesc(Long policyId);

    List<Payment> findByPolicyCustomerIdOrderByDueDateDesc(Long customerId);

    List<Payment> findByStatusAndDueDateBefore(PaymentStatus status, LocalDate date);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("""
            SELECT p FROM Payment p
            WHERE p.policy.customer.id IN (SELECT a.customer.id FROM AgentAssignment a WHERE a.agent.id = :agentId)
            ORDER BY p.dueDate DESC
            """)
    List<Payment> findForAgent(@Param("agentId") Long agentId);
}
