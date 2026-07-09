package com.ims.backend.controller;

import com.ims.backend.dto.policy.PolicyApplyRequest;
import com.ims.backend.dto.policy.PolicyDecisionRequest;
import com.ims.backend.dto.policy.PolicyResponse;
import com.ims.backend.entity.enums.PolicyStatus;
import com.ims.backend.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PolicyResponse> apply(@Valid @RequestBody PolicyApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.apply(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PolicyResponse>> myPolicies() {
        return ResponseEntity.ok(policyService.myPolicies());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<Page<PolicyResponse>> listPolicies(
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(policyService.listPolicies(status, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyResponse> getPolicy(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicy(id));
    }

    @PutMapping("/{id}/decision")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<PolicyResponse> decide(@PathVariable Long id,
                                                 @Valid @RequestBody PolicyDecisionRequest request) {
        return ResponseEntity.ok(policyService.decide(id, request));
    }

    @PostMapping("/{id}/cancel-request")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, String>> requestCancellation(@PathVariable Long id) {
        policyService.requestCancellation(id);
        return ResponseEntity.ok(Map.of("message",
                "Cancellation request submitted. The administrator will process it shortly."));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PolicyResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.cancel(id));
    }
}
