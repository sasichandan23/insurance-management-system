package com.ims.backend.service;

import com.ims.backend.dto.assignment.AssignmentRequest;
import com.ims.backend.dto.assignment.AssignmentResponse;
import com.ims.backend.entity.AgentAssignment;
import com.ims.backend.entity.User;
import com.ims.backend.entity.enums.Role;
import com.ims.backend.exception.BusinessException;
import com.ims.backend.exception.ResourceNotFoundException;
import com.ims.backend.repository.AgentAssignmentRepository;
import com.ims.backend.repository.UserRepository;
import com.ims.backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AgentAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    /** Assigns (or reassigns) an agent to a customer. Admin only. */
    @Transactional
    public AssignmentResponse assign(AssignmentRequest request) {
        User agent = userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent", request.getAgentId()));
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        if (agent.getRole() != Role.AGENT) {
            throw new BusinessException("Selected user is not an agent");
        }
        if (customer.getRole() != Role.CUSTOMER) {
            throw new BusinessException("Selected user is not a customer");
        }

        AgentAssignment assignment = assignmentRepository.findByCustomerId(customer.getId())
                .map(existing -> {
                    existing.setAgent(agent);
                    existing.setAssignedAt(LocalDateTime.now());
                    return existing;
                })
                .orElse(AgentAssignment.builder().agent(agent).customer(customer).build());
        assignment = assignmentRepository.save(assignment);

        notificationService.notify(agent, "New customer assigned",
                "Customer " + customer.getName() + " has been assigned to you.");
        notificationService.notify(customer, "Agent assigned",
                agent.getName() + " is now your insurance agent.");

        return AssignmentResponse.from(assignment);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> listAll() {
        return assignmentRepository.findAll().stream().map(AssignmentResponse::from).toList();
    }

    /** Customers assigned to the currently logged-in agent. */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> myCustomers() {
        User agent = currentUserService.getCurrentUser();
        return assignmentRepository.findByAgentId(agent.getId())
                .stream().map(AssignmentResponse::from).toList();
    }

    @Transactional
    public void unassign(Long assignmentId) {
        AgentAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));
        assignmentRepository.delete(assignment);
    }
}
