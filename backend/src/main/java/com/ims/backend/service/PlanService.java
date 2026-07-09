package com.ims.backend.service;

import com.ims.backend.dto.plan.PlanRequest;
import com.ims.backend.dto.plan.PlanResponse;
import com.ims.backend.entity.Plan;
import com.ims.backend.entity.User;
import com.ims.backend.entity.enums.InsuranceType;
import com.ims.backend.entity.enums.Role;
import com.ims.backend.exception.BusinessException;
import com.ims.backend.exception.ResourceNotFoundException;
import com.ims.backend.repository.PlanRepository;
import com.ims.backend.repository.PolicyRepository;
import com.ims.backend.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final PolicyRepository policyRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<PlanResponse> browse(InsuranceType type, int page, int size) {
        // Customers and agents only see active plans; the admin sees everything
        User user = currentUserService.getCurrentUser();
        boolean activeOnly = user.getRole() != Role.ADMIN;
        return planRepository.browse(activeOnly, type, PageRequest.of(page, size, Sort.by("name")))
                .map(PlanResponse::from);
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlan(Long id) {
        return PlanResponse.from(planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", id)));
    }

    @Transactional
    public PlanResponse create(PlanRequest request) {
        Plan plan = Plan.builder()
                .name(request.getName())
                .insuranceType(request.getInsuranceType())
                .description(request.getDescription())
                .coverageAmount(request.getCoverageAmount())
                .premiumAmount(request.getPremiumAmount())
                .premiumFrequency(request.getPremiumFrequency())
                .durationYears(request.getDurationYears())
                .active(request.isActive())
                .build();
        return PlanResponse.from(planRepository.save(plan));
    }

    @Transactional
    public PlanResponse update(Long id, PlanRequest request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", id));
        plan.setName(request.getName());
        plan.setInsuranceType(request.getInsuranceType());
        plan.setDescription(request.getDescription());
        plan.setCoverageAmount(request.getCoverageAmount());
        plan.setPremiumAmount(request.getPremiumAmount());
        plan.setPremiumFrequency(request.getPremiumFrequency());
        plan.setDurationYears(request.getDurationYears());
        plan.setActive(request.isActive());
        return PlanResponse.from(planRepository.save(plan));
    }

    @Transactional
    public void delete(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", id));
        if (policyRepository.existsByPlanId(id)) {
            throw new BusinessException(
                    "This plan has policies referencing it and cannot be deleted. Deactivate it instead.");
        }
        planRepository.delete(plan);
    }
}
