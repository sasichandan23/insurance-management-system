package com.ims.backend.repository;

import com.ims.backend.entity.AgentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentAssignmentRepository extends JpaRepository<AgentAssignment, Long> {

    Optional<AgentAssignment> findByCustomerId(Long customerId);

    List<AgentAssignment> findByAgentId(Long agentId);

    long countByAgentId(Long agentId);

    boolean existsByAgentIdAndCustomerId(Long agentId, Long customerId);
}
