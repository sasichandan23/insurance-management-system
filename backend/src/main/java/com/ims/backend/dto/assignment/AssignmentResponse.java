package com.ims.backend.dto.assignment;

import com.ims.backend.entity.AgentAssignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private Long id;
    private Long agentId;
    private String agentName;
    private Long customerId;
    private String customerName;
    private LocalDateTime assignedAt;

    public static AssignmentResponse from(AgentAssignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .agentId(assignment.getAgent().getId())
                .agentName(assignment.getAgent().getName())
                .customerId(assignment.getCustomer().getId())
                .customerName(assignment.getCustomer().getName())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }
}
