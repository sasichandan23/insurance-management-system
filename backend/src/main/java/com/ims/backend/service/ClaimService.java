package com.ims.backend.service;

import com.ims.backend.dto.claim.ClaimRequest;
import com.ims.backend.dto.claim.ClaimResponse;
import com.ims.backend.dto.claim.ClaimReviewRequest;
import com.ims.backend.dto.claim.ClaimSettleRequest;
import com.ims.backend.entity.Claim;
import com.ims.backend.entity.Policy;
import com.ims.backend.entity.User;
import com.ims.backend.entity.enums.ClaimStatus;
import com.ims.backend.entity.enums.PolicyStatus;
import com.ims.backend.entity.enums.Role;
import com.ims.backend.exception.BusinessException;
import com.ims.backend.exception.ResourceNotFoundException;
import com.ims.backend.repository.AgentAssignmentRepository;
import com.ims.backend.repository.ClaimRepository;
import com.ims.backend.repository.PolicyRepository;
import com.ims.backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final AgentAssignmentRepository assignmentRepository;
    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    @Transactional
    public ClaimResponse file(ClaimRequest request) {
        User customer = currentUserService.getCurrentUser();
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Policy", request.getPolicyId()));

        if (!policy.getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You can only file claims against your own policies");
        }
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new BusinessException("Claims can only be filed against ACTIVE policies");
        }
        if (request.getClaimAmount().compareTo(policy.getPlan().getCoverageAmount()) > 0) {
            throw new BusinessException("Claim amount cannot exceed the policy coverage amount of "
                    + policy.getPlan().getCoverageAmount());
        }

        Claim claim = Claim.builder()
                .claimNumber(PolicyService.generateNumber("CLM"))
                .policy(policy)
                .claimAmount(request.getClaimAmount())
                .incidentDate(request.getIncidentDate())
                .description(request.getDescription())
                .status(ClaimStatus.FILED)
                .build();
        claim = claimRepository.save(claim);

        Claim saved = claim;
        assignmentRepository.findByCustomerId(customer.getId()).ifPresentOrElse(
                assignment -> notificationService.notify(assignment.getAgent(), "New claim filed",
                        "Customer " + customer.getName() + " filed claim " + saved.getClaimNumber()
                                + " for " + saved.getClaimAmount() + "."),
                () -> notificationService.notifyAdmins("New claim filed",
                        "Customer " + customer.getName() + " filed claim " + saved.getClaimNumber()
                                + " (no agent assigned)."));

        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse review(Long claimId, ClaimReviewRequest request) {
        User actor = currentUserService.getCurrentUser();
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId));

        assertAgentOwnsCustomerOrAdmin(actor, claim.getPolicy().getCustomer().getId());

        if (claim.getStatus() != ClaimStatus.FILED && claim.getStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new BusinessException("Only FILED or UNDER_REVIEW claims can be reviewed");
        }

        ClaimStatus newStatus = ClaimStatus.valueOf(request.getStatus());
        claim.setStatus(newStatus);
        claim.setReviewerRemarks(request.getRemarks());
        claim = claimRepository.save(claim);

        if (newStatus == ClaimStatus.APPROVED) {
            notificationService.notifyAdmins("Claim awaiting settlement",
                    "Claim " + claim.getClaimNumber() + " was approved by the agent and awaits settlement.");
        }
        notificationService.notify(claim.getPolicy().getCustomer(), "Claim " + newStatus.name().toLowerCase().replace('_', ' '),
                "Your claim " + claim.getClaimNumber() + " is now " + newStatus + "."
                        + (request.getRemarks() != null ? " Remarks: " + request.getRemarks() : ""));

        return ClaimResponse.from(claim);
    }

    @Transactional
    public ClaimResponse settle(Long claimId, ClaimSettleRequest request) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId));

        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new BusinessException("Only APPROVED claims can be settled");
        }
        if (request.getSettledAmount().compareTo(claim.getClaimAmount()) > 0) {
            throw new BusinessException("Settled amount cannot exceed the claimed amount");
        }

        claim.setStatus(ClaimStatus.SETTLED);
        claim.setSettledAmount(request.getSettledAmount());
        claim.setSettledDate(LocalDate.now());
        claim = claimRepository.save(claim);

        notificationService.notify(claim.getPolicy().getCustomer(), "Claim settled",
                "Your claim " + claim.getClaimNumber() + " has been settled for " + claim.getSettledAmount() + ".");

        return ClaimResponse.from(claim);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> myClaims() {
        User customer = currentUserService.getCurrentUser();
        return claimRepository.findByPolicyCustomerIdOrderByFiledAtDesc(customer.getId())
                .stream().map(ClaimResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> listClaims(ClaimStatus status, int page, int size) {
        User actor = currentUserService.getCurrentUser();
        PageRequest pageable = PageRequest.of(page, size);
        if (actor.getRole() == Role.AGENT) {
            return claimRepository.findForAgent(actor.getId(), status, pageable).map(ClaimResponse::from);
        }
        return claimRepository.findAllByOptionalStatus(status, pageable).map(ClaimResponse::from);
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaim(Long id) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", id));
        User actor = currentUserService.getCurrentUser();
        if (actor.getRole() == Role.CUSTOMER
                && !claim.getPolicy().getCustomer().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Not your claim");
        }
        if (actor.getRole() == Role.AGENT) {
            assertAgentOwnsCustomerOrAdmin(actor, claim.getPolicy().getCustomer().getId());
        }
        return ClaimResponse.from(claim);
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
}
