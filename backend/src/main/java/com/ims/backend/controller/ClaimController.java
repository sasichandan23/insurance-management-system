package com.ims.backend.controller;

import com.ims.backend.dto.claim.ClaimRequest;
import com.ims.backend.dto.claim.ClaimResponse;
import com.ims.backend.dto.claim.ClaimReviewRequest;
import com.ims.backend.dto.claim.ClaimSettleRequest;
import com.ims.backend.entity.enums.ClaimStatus;
import com.ims.backend.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ClaimResponse> file(@Valid @RequestBody ClaimRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.file(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ClaimResponse>> myClaims() {
        return ResponseEntity.ok(claimService.myClaims());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<Page<ClaimResponse>> listClaims(
            @RequestParam(required = false) ClaimStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(claimService.listClaims(status, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaim(@PathVariable Long id) {
        return ResponseEntity.ok(claimService.getClaim(id));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<ClaimResponse> review(@PathVariable Long id,
                                                @Valid @RequestBody ClaimReviewRequest request) {
        return ResponseEntity.ok(claimService.review(id, request));
    }

    @PutMapping("/{id}/settle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClaimResponse> settle(@PathVariable Long id,
                                                @Valid @RequestBody ClaimSettleRequest request) {
        return ResponseEntity.ok(claimService.settle(id, request));
    }
}
