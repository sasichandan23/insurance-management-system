package com.ims.backend.repository;

import com.ims.backend.entity.Claim;
import com.ims.backend.entity.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByPolicyCustomerIdOrderByFiledAtDesc(Long customerId);

    long countByStatus(ClaimStatus status);

    @Query("""
            SELECT c FROM Claim c
            WHERE c.policy.customer.id IN (SELECT a.customer.id FROM AgentAssignment a WHERE a.agent.id = :agentId)
              AND (:status IS NULL OR c.status = :status)
            ORDER BY c.filedAt DESC
            """)
    Page<Claim> findForAgent(@Param("agentId") Long agentId,
                             @Param("status") ClaimStatus status,
                             Pageable pageable);

    @Query("""
            SELECT c FROM Claim c
            WHERE (:status IS NULL OR c.status = :status)
            ORDER BY c.filedAt DESC
            """)
    Page<Claim> findAllByOptionalStatus(@Param("status") ClaimStatus status, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.status = :status AND c.policy.customer.id IN " +
           "(SELECT a.customer.id FROM AgentAssignment a WHERE a.agent.id = :agentId)")
    long countForAgentByStatus(@Param("agentId") Long agentId, @Param("status") ClaimStatus status);
}
