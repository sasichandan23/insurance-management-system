package com.ims.backend.repository;

import com.ims.backend.entity.Policy;
import com.ims.backend.entity.enums.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    List<Policy> findByCustomerIdOrderByAppliedAtDesc(Long customerId);

    Page<Policy> findByStatus(PolicyStatus status, Pageable pageable);

    long countByStatus(PolicyStatus status);

    boolean existsByPlanId(Long planId);

    List<Policy> findByStatusAndEndDateBefore(PolicyStatus status, LocalDate date);

    @Query("""
            SELECT p FROM Policy p
            WHERE p.customer.id IN (SELECT a.customer.id FROM AgentAssignment a WHERE a.agent.id = :agentId)
              AND (:status IS NULL OR p.status = :status)
            ORDER BY p.appliedAt DESC
            """)
    Page<Policy> findForAgent(@Param("agentId") Long agentId,
                              @Param("status") PolicyStatus status,
                              Pageable pageable);

    @Query("""
            SELECT p FROM Policy p
            WHERE (:status IS NULL OR p.status = :status)
            ORDER BY p.appliedAt DESC
            """)
    Page<Policy> findAllByOptionalStatus(@Param("status") PolicyStatus status, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Policy p WHERE p.status = :status AND p.customer.id IN " +
           "(SELECT a.customer.id FROM AgentAssignment a WHERE a.agent.id = :agentId)")
    long countForAgentByStatus(@Param("agentId") Long agentId, @Param("status") PolicyStatus status);
}
